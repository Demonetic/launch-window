package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.CreateFriendRequest;
import com.launchwindow.dto.FriendshipResponse;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.service.user.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendshipController.class)
@Import(SecurityConfiguration.class)
class FriendshipControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanSendFriendRequest() throws Exception {
        FriendshipResponse response = response(FriendshipStatus.PENDING, true);

        when(service.sendRequest("anna", new CreateFriendRequest("alex"))).thenReturn(response);

        mockMvc.perform(post("/api/friends/requests").with(jwt().jwt(token -> token.subject("anna")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "identifier": "alex"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.username").value("alex"))
                .andExpect(jsonPath("$.avatarKey").value("ALIEN"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestedByCurrentUser").value(true));
    }

    @Test
    void blankFriendRequestIdentifierIsRejected() throws Exception {
        mockMvc.perform(post("/api/friends/requests").with(jwt().jwt(token -> token.subject("anna")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "identifier": " "
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.identifier").value("Username or email is required"));

        verifyNoInteractions(service);
    }

    @Test
    void authenticatedUserCanGetFriends() throws Exception {
        when(service.getFriends("anna")).thenReturn(List.of(response(FriendshipStatus.ACCEPTED, false)));

        mockMvc.perform(get("/api/friends").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    void authenticatedUserCanGetReceivedRequests() throws Exception {
        when(service.getReceivedRequests("anna")).thenReturn(List.of(response(FriendshipStatus.PENDING, false)));

        mockMvc.perform(get("/api/friends/requests/received").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].requestedByCurrentUser").value(false));
    }

    @Test
    void authenticatedUserCanGetSentRequests() throws Exception {
        when(service.getSentRequests("anna")).thenReturn(List.of(response(FriendshipStatus.PENDING, true)));

        mockMvc.perform(get("/api/friends/requests/sent").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].requestedByCurrentUser").value(true));
    }

    @Test
    void authenticatedUserCanAcceptFriendRequest() throws Exception {
        when(service.accept("anna", 10L)).thenReturn(response(FriendshipStatus.ACCEPTED, false));

        mockMvc.perform(patch("/api/friends/requests/10/accept").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(service).accept("anna", 10L);
    }

    @Test
    void authenticatedUserCanDeclineFriendRequest() throws Exception {
        when(service.decline("anna", 10L)).thenReturn(response(FriendshipStatus.DECLINED, false));

        mockMvc.perform(patch("/api/friends/requests/10/decline").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));

        verify(service).decline("anna", 10L);
    }

    @Test
    void authenticatedUserCanRemoveFriendship() throws Exception {
        mockMvc.perform(delete("/api/friends/10").with(jwt().jwt(token -> token.subject("anna"))))
                .andExpect(status().isNoContent());

        verify(service).remove("anna", 10L);
    }

    @Test
    void anonymousUserCannotAccessFriends() throws Exception {
        mockMvc.perform(get("/api/friends")).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private FriendshipResponse response(FriendshipStatus status, boolean requestedByCurrentUser) {
        return new FriendshipResponse(
                10L,
                2L,
                "alex",
                AvatarKey.ALIEN,
                "#8FE8C3",
                status,
                requestedByCurrentUser,
                Instant.parse("2026-07-25T17:00:00Z"),
                status == FriendshipStatus.PENDING
                        ? null
                        : Instant.parse(
                        "2026-07-25T18:00:00Z"
                )
        );
    }
}