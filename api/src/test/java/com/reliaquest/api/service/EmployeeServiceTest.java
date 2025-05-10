package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exceptions.BadUUIDException;
import com.reliaquest.api.model.DataWrapper;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

class EmployeeServiceTest {

    private EmployeeService employeeService;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    private WebClient.ResponseSpec responseSpecMock;

    @BeforeEach
    void setUp() {
        WebClient webClientMock = mock(WebClient.class);
        requestHeadersUriSpecMock = mock(RequestHeadersUriSpec.class);
        requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        responseSpecMock = mock(ResponseSpec.class);

        WebClient.Builder webClientBuilderMock = mock(WebClient.Builder.class);
        when(webClientBuilderMock.baseUrl(anyString())).thenReturn(webClientBuilderMock);
        when(webClientBuilderMock.build()).thenReturn(webClientMock);

        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

        // Mock the onStatus method to return the same ResponseSpec mock
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);

        employeeService = new EmployeeService(webClientBuilderMock);
    }

    @Test
    void getAllEmployees() {
        List<Employee> mockEmployees = List.of(new Employee("1", "John Doe", 50000));
        DataWrapper<List<Employee>> mockResponse = new DataWrapper<>(mockEmployees);

        when(responseSpecMock.bodyToMono(new ParameterizedTypeReference<DataWrapper<List<Employee>>>() {}))
                .thenReturn(Mono.just(mockResponse));

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getEmployeeName());
    }

    @Test
    void getEmployeeById() {
        Employee mockEmployee = new Employee("1", "John Doe", 50000);
        DataWrapper<Employee> mockResponse = new DataWrapper<>(mockEmployee);

        when(requestHeadersUriSpecMock.uri(anyString(), any(UUID.class))).thenReturn(requestHeadersSpecMock);
        when(responseSpecMock.bodyToMono(new ParameterizedTypeReference<DataWrapper<Employee>>() {}))
                .thenReturn(Mono.just(mockResponse));

        Employee employee = employeeService.getEmployeeById(UUID.randomUUID().toString());

        assertNotNull(employee);
        assertEquals("John Doe", employee.getEmployeeName());
    }

    @Test
    void getEmployeeById_InvalidUUID() {
        assertThrows(BadUUIDException.class, () -> employeeService.getEmployeeById("invalid-uuid"));
    }

    @Test
    void getEmployeeByNameSearch() {
        List<Employee> mockEmployees =
                List.of(new Employee("1", "John Doe", 50000), new Employee("2", "Jane Smith", 60000));
        DataWrapper<List<Employee>> mockResponse = new DataWrapper<>(mockEmployees);

        when(responseSpecMock.bodyToMono(new ParameterizedTypeReference<DataWrapper<List<Employee>>>() {}))
                .thenReturn(Mono.just(mockResponse));

        List<Employee> employees = employeeService.getEmployeeByNameSearch("Jane");

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("Jane Smith", employees.get(0).getEmployeeName());
    }

    @Test
    void getHighestSalaryOfEmployees() {
        List<Employee> mockEmployees =
                List.of(new Employee("1", "John Doe", 50000), new Employee("2", "Jane Smith", 60000));
        DataWrapper<List<Employee>> mockResponse = new DataWrapper<>(mockEmployees);

        when(responseSpecMock.bodyToMono(new ParameterizedTypeReference<DataWrapper<List<Employee>>>() {}))
                .thenReturn(Mono.just(mockResponse));

        int highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertEquals(60000, highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() {
        List<Employee> mockEmployees = List.of(
                new Employee("1", "John Doe", 50000),
                new Employee("2", "Jane Smith", 60000),
                new Employee("3", "Alice Brown", 70000));
        DataWrapper<List<Employee>> mockResponse = new DataWrapper<>(mockEmployees);

        when(responseSpecMock.bodyToMono(new ParameterizedTypeReference<DataWrapper<List<Employee>>>() {}))
                .thenReturn(Mono.just(mockResponse));

        List<String> topEarningNames = employeeService.getTopTenHighestEarningEmployeeNames();

        assertNotNull(topEarningNames);
        assertEquals(3, topEarningNames.size());
        assertEquals("Alice Brown", topEarningNames.get(0));
    }
}
