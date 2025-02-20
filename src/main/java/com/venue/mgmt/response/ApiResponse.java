package com.venue.mgmt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
@Data
@NoArgsConstructor
public class ApiResponse<T> {
    
    @JsonProperty("responseCode")
    private int responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
    
    @JsonProperty("data")
    private T data;

    public ApiResponse(int responseCode, String responseMessage, T data) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.data = data;
    }

    public static <T> ApiResponse<T> createResponseWithData(T data, String message) {
        return new ApiResponse<T>(HttpStatus.OK.value(), message, data);
    }
}