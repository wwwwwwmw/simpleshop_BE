package com.example.localizationtesting.dto;

public record AuthMeResponse(
        String email,
        String fullName,
        String userRole) {
}
