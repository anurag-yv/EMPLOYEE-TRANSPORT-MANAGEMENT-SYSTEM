package com.example.employee_transport_system.dto;


public class AuthResponse {
    private String token;
    private String refreshToken;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public AuthResponse(final String token) {
        this.token = token;
    }

    public AuthResponse(final String token, final String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public AuthResponse() {
    }
}
