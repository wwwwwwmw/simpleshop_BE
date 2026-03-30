package com.example.localizationtesting.dto;

public record AuthResponse(
        String accessToken,
        String userRole,
        String email,
        String fullName) {
}
