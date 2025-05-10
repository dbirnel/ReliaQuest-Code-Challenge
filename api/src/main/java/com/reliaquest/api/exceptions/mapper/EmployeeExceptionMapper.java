package com.reliaquest.api.exceptions.mapper;

import com.reliaquest.api.exceptions.BadUUIDException;
import com.reliaquest.api.exceptions.NotFoundException;
import com.reliaquest.api.model.DataWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmployeeExceptionMapper {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<DataWrapper<String>> handleNotFoundException(NotFoundException exception) {
        DataWrapper<String> response = new DataWrapper<>();
        response.setStatus("error");
        response.setError(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadUUIDException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<DataWrapper<String>> handleBadUUIDException(BadUUIDException exception) {
        DataWrapper<String> response = new DataWrapper<>();
        response.setStatus("error");
        response.setError(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
