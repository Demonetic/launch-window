package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Role;
import com.launchwindow.service.user.UserAvatarService;
import com.launchwindow.service.user.UserDeletionService;
import com.launchwindow.service.user.UserQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService service;

    @MockitoBean
    private UserAvatarService avatarService;

    @MockitoBean
    private UserDeletionService deletionService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanGetOwnProfile() throws Exception {
        UserResponse user = new UserResponse(
                1L,
                "launch_test",
                "launch-test@example.com",
                Role.USER,
                AvatarKey.ASTRONAUT,
                "#FFFFFF"
        );

        when(service.getUser("launch_test")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me").with(jwt().jwt(token -> token
                                .subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("launch_test"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.avatarKey").value("ASTRONAUT"))
                .andExpect(jsonPath("$.avatarColor").value("#FFFFFF"));
    }

    @Test
    void anonymousUserCannotGetProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenUserReturnsNotFound() throws Exception {
        when(service.getUser("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(token -> token.subject("missing"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Authenticated user was not found"))
                .andExpect(jsonPath("$.path").value("/api/users/me"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}