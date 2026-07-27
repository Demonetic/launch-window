package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.DeleteAccountRequest;
import com.launchwindow.dto.UpdateAvatarRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.dto.UserStatisticsResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.service.user.UserAvatarService;
import com.launchwindow.service.user.UserDeletionService;
import com.launchwindow.service.user.UserQueryService;
import com.launchwindow.service.user.UserStatisticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class UserController {
    private final UserQueryService queryService;
    private final UserAvatarService avatarService;
    private final UserDeletionService deletionService;
    private final UserStatisticsService statisticsService;

    public UserController(UserQueryService queryService, UserAvatarService avatarService, UserDeletionService deletionService,
                          UserStatisticsService statisticsService) {
        this.queryService = queryService;
        this.avatarService = avatarService;
        this.deletionService = deletionService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return queryService.getUser(jwt.getSubject()).orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    @GetMapping("/me/statistics")
    public UserStatisticsResponse getCurrentUserStatistics(@AuthenticationPrincipal Jwt jwt) {
        return statisticsService.getStatistics(jwt.getSubject());
    }

    @PatchMapping("/me/avatar")
    public UserResponse updateAvatar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateAvatarRequest request) {
        return avatarService.updateAvatar(jwt.getSubject(), request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeleteAccountRequest request) {
        deletionService.deleteAccount(jwt.getSubject(), request);

        return ResponseEntity.noContent().build();
    }
}