package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.notification.NotificationResponse;
import com.launchwindow.dto.notification.UnreadNotificationCountResponse;
import com.launchwindow.model.*;
import com.launchwindow.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfiguration.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanGetNotifications() throws Exception {
        when(service.getLatest("anna")).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/notifications").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].type").value("CALENDAR_INVITATION_RECEIVED"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].actorId").value(2))
                .andExpect(jsonPath("$[0].actorUsername").value("alex"))
                .andExpect(jsonPath("$[0].actorAvatarKey").value("ALIEN"))
                .andExpect(jsonPath("$[0].calendarInvitationId").value(30))
                .andExpect(jsonPath("$[0].calendarInvitationStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].launchId").value(40))
                .andExpect(jsonPath("$[0].launchName").value("Test launch"));
    }

    @Test
    void authenticatedUserCanGetUnreadCount() throws Exception {
        when(service.getUnreadCount("anna")).thenReturn(new UnreadNotificationCountResponse(3L));

        mockMvc.perform(get("/api/notifications/unread-count").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    void authenticatedUserCanMarkNotificationRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/10/read").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isNoContent());

        verify(service).markRead("anna", 10L);
    }

    @Test
    void authenticatedUserCanMarkAllNotificationsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isNoContent());

        verify(service).markAllRead("anna");
    }

    @Test
    void anonymousUserCannotAccessNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private NotificationResponse response() {
        return new NotificationResponse(
                10L,
                NotificationType.CALENDAR_INVITATION_RECEIVED,
                false,
                Instant.parse("2026-07-27T12:00:00Z"),
                2L,
                "alex",
                AvatarKey.ALIEN,
                "#8FE8C3",
                null,
                null,
                30L,
                CalendarInvitationStatus.PENDING,
                40L,
                "Test launch",
                Instant.parse("2026-08-01T10:00:00Z")
        );
    }
}