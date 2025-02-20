package com.venue.mgmt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


public class ValidateUserResponse {
    private Integer statusCode;
    private String statusMsg;
    private String errorMsg;
    private ResponseData response;

    public ValidateUserResponse() {
    }

    public ValidateUserResponse(Integer statusCode, String statusMsg, String errorMsg, ResponseData response) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
        this.errorMsg = errorMsg;
        this.response = response;
    }

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

    public static class ResponseData {
        private String refId;
        private String mobileNumber;
        private String userid;
        private String expiryTimer;
        private TokenDetails tokenDetails;

        public ResponseData() {
        }

        public ResponseData(String refId, String mobileNumber, String userid, String expiryTimer, TokenDetails tokenDetails) {
            this.refId = refId;
            this.mobileNumber = mobileNumber;
            this.userid = userid;
            this.expiryTimer = expiryTimer;
            this.tokenDetails = tokenDetails;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getExpiryTimer() {
            return expiryTimer;
        }

        public void setExpiryTimer(String expiryTimer) {
            this.expiryTimer = expiryTimer;
        }

        public TokenDetails getTokenDetails() {
            return tokenDetails;
        }

        public void setTokenDetails(TokenDetails tokenDetails) {
            this.tokenDetails = tokenDetails;
        }
    }

    public static class TokenDetails {
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;

        public TokenDetails() {
        }

        public TokenDetails(String accessToken, String refreshToken, Integer expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

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
