package com.example.localizationtesting.dto;

public record AuthRegisterRequest(
        String fullName,
        String email,
        String password) {
}
