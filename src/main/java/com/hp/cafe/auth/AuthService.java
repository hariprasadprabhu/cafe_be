package com.hp.cafe.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        validateSignup(request);
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User(request.name().trim(), email,
            passwordEncoder.encode(request.password()));
        users.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        if (request.email() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        User user = users.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse me(String userId) {
        User user = users.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token"));
        return new AuthDtos.UserResponse(user.getId().toString(), user.getName(), user.getEmail());
    }

    @Transactional
    public AuthDtos.AuthResponse refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        RefreshToken stored = refreshTokens.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        stored.revoke();
        return issueTokens(stored.getUser());
    }

    private AuthDtos.AuthResponse issueTokens(User user) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken refreshToken = new RefreshToken(hash(rawToken), user,
            Instant.now().plusSeconds(30L * 24 * 60 * 60));
        refreshTokens.save(refreshToken);
        AuthDtos.UserResponse responseUser = new AuthDtos.UserResponse(
            user.getId().toString(), user.getName(), user.getEmail());
        return new AuthDtos.AuthResponse(jwtService.createAccessToken(user), rawToken, responseUser);
    }

    private void validateSignup(AuthDtos.SignupRequest request) {
        if (request == null || request.name() == null || request.email() == null || request.password() == null
            || request.name().isBlank() || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and email are required");
        }
        if (request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : bytes) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
