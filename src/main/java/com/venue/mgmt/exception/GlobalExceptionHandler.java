package com.venue.mgmt.exception;

import com.venue.mgmt.constant.ErrorMsgConstants;
import org.apache.coyote.BadRequestException;
import org.json.JSONObject;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VenueAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleVenueAlreadyExistsException(VenueAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", errorMsg);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "The server is not running or cannot be reached.");
    }

    @ExceptionHandler(LeadNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleLeadNotFoundException(LeadNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistException(EmailAlreadyExistException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Email Already Exists.", ex.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Map<String, Object>> handleExceptions(Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ErrorMsgConstants.ERROR_INTERNAL_SERVER;

        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.NOT_ACCEPTABLE;
            message = "You are not authorized to access this request";
        }
        else if(ex instanceof HttpClientErrorException) {
            status = HttpStatus.BAD_REQUEST;
            String rawResponse = ex.getMessage();
            int startIndex = rawResponse.indexOf("{");
            int endIndex = rawResponse.lastIndexOf("}") + 1;
            if (startIndex != -1 && endIndex != -1) {
                String jsonResponse = rawResponse.substring(startIndex, endIndex);
                jsonResponse=jsonResponse.replace("<EOL>", "\n");
                JSONObject jsonObject = new JSONObject(jsonResponse);
                message = jsonObject.getString("errorMsg");
            }
        }
        else if(ex instanceof HttpServerErrorException.BadGateway) {
            status = HttpStatus.BAD_GATEWAY;
            message = "Bad Gateway";
        }
        else if (ex instanceof HttpStatusException httpEx) {
            status = httpEx.getCode();
            message = (httpEx.getReason() != null && !httpEx.getReason().isEmpty())
                    ? httpEx.getReason()
                    : message;
        } else if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
            status = annotation.code();
            if (!annotation.reason().isEmpty()) {
                message = annotation.reason();
            }
        }
        return buildErrorResponse(status, "Internal Server Error", message);
    }


    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleAlreadyExistsException(AlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getReason());
    }

    @ExceptionHandler(VenueNotSavedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleVenueNotSavedException(VenueNotSavedException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    @ExceptionHandler(InvalidOtpException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleInvalidOtpException(InvalidOtpException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Invalid OTP", ex.getMessage());
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