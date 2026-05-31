package com.carddemo.backend.auth.dto;

public class LoginResponse {

    private String token;
    private String userId;
    private String userType;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String userType) {
        this.token = token;
        this.userId = userId;
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
