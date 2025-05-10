package com.reliaquest.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataWrapper<T> {
    private String status;
    private String error;
    private T data;

    public DataWrapper(T data) {
        this.data = data;
    }
}
