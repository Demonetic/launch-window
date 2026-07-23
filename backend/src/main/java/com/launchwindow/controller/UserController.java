package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.UpdateAvatarRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.service.user.UserAvatarService;
import com.launchwindow.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class UserController {
    private final UserQueryService queryService;
    private final UserAvatarService avatarService;

    public UserController(UserQueryService queryService, UserAvatarService avatarService) {
        this.queryService = queryService;
        this.avatarService = avatarService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return queryService.getUser(jwt.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    @PatchMapping("/me/avatar")
    public UserResponse updateAvatar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateAvatarRequest request) {
        return avatarService.updateAvatar(jwt.getSubject(), request);
    }
}
