package com.launchwindow.controller;

import com.launchwindow.dto.*;
import com.launchwindow.service.LoginService;
import com.launchwindow.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final LoginService loginService;

    public AuthController(RegistrationService registrationService, LoginService loginService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}