package com.example.localizationtesting.service.impl;

import com.example.localizationtesting.dto.AuthLoginRequest;
import com.example.localizationtesting.dto.AuthMeResponse;
import com.example.localizationtesting.dto.AuthRegisterRequest;
import com.example.localizationtesting.dto.AuthResponse;
import com.example.localizationtesting.entity.User;
import com.example.localizationtesting.exception.UnauthorizedException;
import com.example.localizationtesting.repository.UserRepository;
import com.example.localizationtesting.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        validateRegisterRequest(request);

        userRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already exists.");
                });

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(ROLE_CUSTOMER);

        User saved = userRepository.save(user);
        String token = createSession(saved.getId());
        return new AuthResponse(token, saved.getRole(), saved.getEmail(), saved.getFullName());
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        String token = createSession(user.getId());
        return new AuthResponse(token, user.getRole(), user.getEmail(), user.getFullName());
    }

    @Override
    public AuthMeResponse me(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        Long userId = sessions.get(token);
        if (userId == null) {
            throw new UnauthorizedException("Invalid token.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found for session."));

        return new AuthMeResponse(user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        sessions.remove(token);
    }

    private void validateRegisterRequest(AuthRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Register payload is required.");
        }

        if (isBlank(request.fullName()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Full name, email and password are required.");
        }

        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        if (request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private String createSession(Long userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        return token;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authorization header is required.");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must use Bearer token.");
        }

        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
