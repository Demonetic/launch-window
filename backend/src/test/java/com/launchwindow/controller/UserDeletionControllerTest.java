package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.DeleteAccountRequest;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.service.user.UserAvatarService;
import com.launchwindow.service.user.UserDeletionService;
import com.launchwindow.service.user.UserQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserDeletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService queryService;

    @MockitoBean
    private UserAvatarService avatarService;

    @MockitoBean
    private UserDeletionService deletionService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanDeleteOwnAccount() throws Exception {
        mockMvc.perform(delete("/api/users/me").with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "current-password"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deletionService).deleteAccount("launch_test", new DeleteAccountRequest("current-password"));
    }

    @Test
    void incorrectPasswordReturnsUnauthorized() throws Exception {
        doThrow(new InvalidCredentialsException("Password is incorrect")).when(deletionService).deleteAccount("launch_test",
                new DeleteAccountRequest("wrong-password"));

        mockMvc.perform(delete("/api/users/me").with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Password is incorrect"))
                .andExpect(jsonPath("$.path").value("/api/users/me"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void missingPasswordReturnsValidationError() throws Exception {
        mockMvc.perform(delete("/api/users/me").with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"));
    }

    @Test
    void anonymousUserCannotDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "current-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}