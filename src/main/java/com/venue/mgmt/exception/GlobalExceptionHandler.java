package com.venue.mgmt.exception;

import org.apache.coyote.BadRequestException;
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
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors.toString());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientErrorException(HttpClientErrorException ex) {
        String responseBody = ex.getResponseBodyAsString();
        JSONObject jsonResponse = new JSONObject(responseBody);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", jsonResponse.getString("message"));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleAlreadyExistsException(AlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String statusMsg, String errorMsg) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("statusCode", status.value());
        responseBody.put("statusMsg", statusMsg);
        responseBody.put("errorMsg", errorMsg);
        responseBody.put("response", null);
        return new ResponseEntity<>(responseBody, status);
    }
}