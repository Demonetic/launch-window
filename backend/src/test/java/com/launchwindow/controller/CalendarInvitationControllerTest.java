package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.CalendarInvitationResponse;
import com.launchwindow.dto.CreateCalendarInvitationRequest;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.service.calendar.CalendarInvitationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarInvitationController.class)
@Import(SecurityConfiguration.class)
class CalendarInvitationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarInvitationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanInviteAnotherUser() throws Exception {
        CalendarInvitationResponse response = response(CalendarInvitationStatus.PENDING, null);

        when(service.invite(eq("anna"), eq(10L), eq(new CreateCalendarInvitationRequest("alex"))))
                .thenReturn(response);

        mockMvc.perform(post("/api/calendar/10/invitations").with(jwt().jwt(token -> token.subject("anna")))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "identifier": "alex"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.launchId").value(10))
                .andExpect(jsonPath("$.launchName").value("Test launch"))
                .andExpect(jsonPath("$.inviterUsername").value("anna"))
                .andExpect(jsonPath("$.inviterAvatarKey").value("ASTRONAUT"))
                .andExpect(jsonPath("$.inviterAvatarColor").value("#FFFFFF"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void authenticatedUserCanGetPendingInvitations() throws Exception {
        when(service.getPendingInvitations("alex"))
                .thenReturn(List.of(response(CalendarInvitationStatus.PENDING, null)));

        mockMvc.perform(get("/api/calendar/invitations/pending").with(jwt().jwt(token -> token.subject("alex"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].launchId").value(10))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void authenticatedUserCanAcceptInvitation() throws Exception {
        when(service.accept("alex", 50L))
                .thenReturn(response(CalendarInvitationStatus.ACCEPTED, Instant.parse("2026-07-23T20:00:00Z")));

        mockMvc.perform(patch("/api/calendar/invitations/50/accept").with(jwt().jwt(token -> token.subject("alex"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.respondedAt").value("2026-07-23T20:00:00Z"));
    }

    @Test
    void authenticatedUserCanDeclineInvitation() throws Exception {
        when(service.decline("alex", 50L))
                .thenReturn(response(CalendarInvitationStatus.DECLINED, Instant.parse("2026-07-23T20:00:00Z")));

        mockMvc.perform(patch("/api/calendar/invitations/50/decline").with(jwt().jwt(token -> token.subject("alex"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void blankInvitationIdentifierReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/calendar/10/invitations").with(jwt().jwt(token -> token.subject("anna")))
                                .contentType("application/json")
                                .content("""
                                        {
                                          "identifier": " "
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.identifier").isNotEmpty());
    }

    @Test
    void anonymousUserCannotUseInvitationEndpoints() throws Exception {
        mockMvc.perform(get("/api/calendar/invitations/pending")).andExpect(status().isUnauthorized());
    }

    private CalendarInvitationResponse response(CalendarInvitationStatus status, Instant respondedAt) {
        return new CalendarInvitationResponse(
                50L,
                10L,
                "Test launch",
                Instant.parse(
                        "2026-08-01T10:00:00Z"
                ),
                1L,
                "anna",
                AvatarKey.ASTRONAUT,
                "#FFFFFF",
                status,
                Instant.parse(
                        "2026-07-23T19:00:00Z"
                ),
                respondedAt
        );
    }
}