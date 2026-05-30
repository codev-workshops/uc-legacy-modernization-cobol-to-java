package com.mainframe.carddemo.auth.dto;

public class LoginResponse {

    private String token;
    private String userId;
    private String userType;
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String userType, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.userType = userType;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
