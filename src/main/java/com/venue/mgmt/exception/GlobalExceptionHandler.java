package com.venue.mgmt.exception;

import com.venue.mgmt.response.ErrorResponse;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(HttpClientErrorException ex) {
        String responseBody = ex.getResponseBodyAsString();
        JSONObject jsonResponse = new JSONObject(responseBody);
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                jsonResponse.get("statusMsg").toString(),
               jsonResponse.get("errorMsg").toString(),
               null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}