package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.notification.NotificationResponse;
import com.launchwindow.dto.notification.UnreadNotificationCountResponse;
import com.launchwindow.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> getLatest(@AuthenticationPrincipal Jwt jwt) {
        return service.getLatest(jwt.getSubject());
    }

    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        return service.getUnreadCount(jwt.getSubject());
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long notificationId) {
        service.markRead(jwt.getSubject(), notificationId);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        service.markAllRead(jwt.getSubject());
    }
}