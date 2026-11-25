package com.hp.cafe.auth;

public class AuthDtos {
    public record SignupRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record ProfileRequest(String dob, String mobile) {}
    public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
    public record UserResponse(String id, String name, String email, String dob, String mobile) {}
    public record ErrorResponse(String error) {}
    public record PushTokenRequest(String pushToken) {}
}
