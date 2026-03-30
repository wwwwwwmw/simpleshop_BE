package com.example.localizationtesting.service;

import com.example.localizationtesting.dto.AuthLoginRequest;
import com.example.localizationtesting.dto.AuthMeResponse;
import com.example.localizationtesting.dto.AuthRegisterRequest;
import com.example.localizationtesting.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);

    AuthMeResponse me(String authorizationHeader);

    void logout(String authorizationHeader);
}
