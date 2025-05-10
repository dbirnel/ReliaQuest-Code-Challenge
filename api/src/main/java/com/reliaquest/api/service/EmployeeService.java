package com.reliaquest.api.service;

import com.reliaquest.api.exceptions.BadUUIDException;
import com.reliaquest.api.exceptions.NotFoundException;
import com.reliaquest.api.model.DataWrapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.RemoveWrapper;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EmployeeService {

    private final WebClient webClient;

    public EmployeeService(WebClient.Builder webClientBuilder) {
        this.webClient =
                webClientBuilder.baseUrl("http://localhost:8112/api/v1").build();
    }

    @Cacheable(value = "employees")
    public List<Employee> getAllEmployees() {
        DataWrapper<List<Employee>> result = webClient
                .get()
                .uri("/employee")
                .retrieve()
                .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS, clientResponse -> {
                    String retryAfter = clientResponse.headers().asHttpHeaders().getFirst("Retry-After");
                    System.err.println("Too Many Requests. Retry after: " + retryAfter);
                    return Mono.error(new RuntimeException("Too Many Requests. Please retry after: " + retryAfter));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Error fetching all employees");
                            System.err.println("Status Code: " + clientResponse.statusCode());
                            System.err.println("Error Body: " + errorBody);
                            return Mono.error(
                                    new RuntimeException("Failed to fetch all employees. Server responded with: "
                                            + clientResponse.statusCode()));
                        }))
                .bodyToMono(new ParameterizedTypeReference<DataWrapper<List<Employee>>>() {})
                .block();
        return result != null ? result.getData() : List.of();
    }

    @Cacheable(value = "employeeById", key = "#id")
    public Employee getEmployeeById(String id) {
        try {
            // Validate and convert the id to a UUID
            UUID uuid = UUID.fromString(id);

            DataWrapper<Employee> result = webClient
                    .get()
                    .uri("/employee/{id}", uuid)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS, clientResponse -> {
                        String retryAfter =
                                clientResponse.headers().asHttpHeaders().getFirst("Retry-After");
                        System.err.println("Too Many Requests. Retry after: " + retryAfter);
                        return Mono.error(new RuntimeException("Too Many Requests. Please retry after: " + retryAfter));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse
                            .bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.err.println("Error fetching employee with ID: " + id);
                                System.err.println("Status Code: " + clientResponse.statusCode());
                                System.err.println("Error Body: " + errorBody);
                                return Mono.error(new RuntimeException("Failed to fetch employee with ID: " + id
                                        + ". Server responded with: " + clientResponse.statusCode()));
                            }))
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> {
                        throw new NotFoundException("Employee not found");
                    })
                    .bodyToMono(new ParameterizedTypeReference<DataWrapper<Employee>>() {})
                    .block();

            return result != null ? result.getData() : null;
        } catch (IllegalArgumentException e) {
            throw new BadUUIDException("Invalid ID format. Expected a UUID but received: " + id);
        }
    }

    public List<Employee> getEmployeeByNameSearch(String searchString) {
        return this.getAllEmployees().stream()
                .filter(x -> x.getEmployeeName().contains(searchString))
                .toList();
    }

    public Integer getHighestSalaryOfEmployees() {
        return this.getAllEmployees().stream()
                .map(Employee::getEmployeeSalary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        return this.getAllEmployees().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getEmployeeSalary(), e1.getEmployeeSalary()))
                .map(Employee::getEmployeeName)
                .limit(10)
                .toList();
    }

    public Employee createEmployee(EmployeeInput employeeInput) {
        DataWrapper<Employee> result = webClient
                .post()
                .uri("/employee")
                .bodyValue(employeeInput)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataWrapper<Employee>>() {})
                .block();
        return result != null ? result.getData() : null;
    }

    public String deleteEmployeeById(String id) {
        Employee deleteMe = this.getAllEmployees().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        DataWrapper<String> result = webClient
                .method(HttpMethod.DELETE)
                .uri("/employee")
                .body(Mono.just(new RemoveWrapper(deleteMe.getEmployeeName())), RemoveWrapper.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataWrapper<String>>() {})
                .block();
        return result != null ? result.getData() : null;
    }
}
