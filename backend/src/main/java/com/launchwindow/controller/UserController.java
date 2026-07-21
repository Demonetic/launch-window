package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class UserController {
    private final UserQueryService service;

    public UserController(UserQueryService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return service.getUser(jwt.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }
}
