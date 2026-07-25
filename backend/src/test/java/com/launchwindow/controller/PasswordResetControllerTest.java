package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.ForgotPasswordRequest;
import com.launchwindow.dto.ResetPasswordRequest;
import com.launchwindow.exception.InvalidPasswordResetTokenException;
import com.launchwindow.service.auth.LoginService;
import com.launchwindow.service.auth.PasswordResetService;
import com.launchwindow.service.auth.RegistrationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class PasswordResetControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RegistrationService registrationService;
    @MockitoBean
    private LoginService loginService;
    @MockitoBean
    private PasswordResetService passwordResetService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void anonymousUserCanRequestPasswordReset() throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "space@example.com"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(passwordResetService).requestPasswordReset(new ForgotPasswordRequest("space@example.com"));
    }

    @Test
    void invalidEmailReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"));
    }

    @Test
    void anonymousUserCanResetPassword() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "valid-token",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(passwordResetService).resetPassword(new ResetPasswordRequest("valid-token", "new-password"));
    }

    @Test
    void invalidResetTokenReturnsBadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "new-password");

        doThrow(new InvalidPasswordResetTokenException()).when(passwordResetService).resetPassword(request);

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "invalid-token",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD_RESET_TOKEN"))
                .andExpect(jsonPath("$.message").value("Password reset link is invalid or has expired"))
                .andExpect(jsonPath("$.path").value("/api/auth/password/reset"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shortNewPasswordReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "valid-token",
                                  "newPassword": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.newPassword").value("Password must be between 8 and 72 characters"));
    }
}