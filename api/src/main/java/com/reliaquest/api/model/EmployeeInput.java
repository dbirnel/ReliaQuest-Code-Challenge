package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class EmployeeInput {
    @NotBlank
    @JsonProperty("name")
    private String name;

    @NotNull @Positive @JsonProperty("salary")
    private int salary;

    @Min(16)
    @Max(75)
    @NotNull @JsonProperty("age")
    private int age;

    @NotBlank
    @JsonProperty("title")
    private String title;
}
