package com.example.localizationtesting.controller;

import com.example.localizationtesting.dto.AuthLoginRequest;
import com.example.localizationtesting.dto.AuthMeResponse;
import com.example.localizationtesting.dto.AuthRegisterRequest;
import com.example.localizationtesting.dto.AuthResponse;
import com.example.localizationtesting.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@RequestHeader("Authorization") String authorizationHeader) {
        return authService.me(authorizationHeader);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
    }
}
