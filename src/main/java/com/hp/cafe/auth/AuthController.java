package com.hp.cafe.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.AuthResponse> signup(@RequestBody AuthDtos.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(@AuthenticationPrincipal String userId) {
        return authService.me(userId);
    }

    @PostMapping("/refresh")
    public AuthDtos.AuthResponse refresh(@RequestBody AuthDtos.RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PutMapping("/profile")
    public AuthDtos.UserResponse updateProfile(@AuthenticationPrincipal String userId,
                                               @RequestBody AuthDtos.ProfileRequest request) {
        return authService.updateProfile(userId, request);
    }

    @PutMapping("/push-token")
    public void updatePushToken(@AuthenticationPrincipal String userId,
                                @RequestBody AuthDtos.PushTokenRequest request) {
        authService.updatePushToken(userId, request);
    }
}
