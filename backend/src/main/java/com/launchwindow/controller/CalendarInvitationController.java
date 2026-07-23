package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.CalendarInvitationResponse;
import com.launchwindow.dto.CreateCalendarInvitationRequest;
import com.launchwindow.service.calendar.CalendarInvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class CalendarInvitationController {
    private final CalendarInvitationService service;

    public CalendarInvitationController(CalendarInvitationService service) {
        this.service = service;
    }

    @PostMapping("/{launchId}/invitations")
    public CalendarInvitationResponse invite(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId,
                                             @Valid @RequestBody CreateCalendarInvitationRequest request) {
        return service.invite(jwt.getSubject(), launchId, request);
    }

    @GetMapping("/invitations/pending")
    public List<CalendarInvitationResponse>
    getPendingInvitations(@AuthenticationPrincipal Jwt jwt) {
        return service.getPendingInvitations(jwt.getSubject());
    }

    @PatchMapping("/invitations/{invitationId}/accept")
    public CalendarInvitationResponse accept(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invitationId) {
        return service.accept(jwt.getSubject(), invitationId);
    }

    @PatchMapping("/invitations/{invitationId}/decline")
    public CalendarInvitationResponse decline(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invitationId) {
        return service.decline(jwt.getSubject(), invitationId);
    }
}