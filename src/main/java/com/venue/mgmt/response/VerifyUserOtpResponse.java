package com.venue.mgmt.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VerifyUserOtpResponse {
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    @JsonProperty("statusMsg")
    private String statusMsg;
    
    @JsonProperty("errorMsg")
    private String errorMsg;
    
    @JsonProperty("response")
    private ResponseData response;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ResponseData getResponse() {
        return response;
    }

    public void setResponse(ResponseData response) {
        this.response = response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ResponseData {
        @JsonProperty("tokenResponse")
        private TokenDetails tokenDetails;

        public TokenDetails getTokenDetails() {
            return tokenDetails;
        }

        public void setTokenDetails(TokenDetails tokenDetails) {
            this.tokenDetails = tokenDetails;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class TokenDetails {
        @JsonProperty("accessToken")
        private String accessToken;
        
        @JsonProperty("refreshToken")
        private String refreshToken;
        
        @JsonProperty("expiresIn")
        private Integer expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public Integer getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}
