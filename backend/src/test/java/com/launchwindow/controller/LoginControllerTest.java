package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LoginRequest;
import com.launchwindow.dto.LoginResponse;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.GlobalExceptionHandler;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.model.Role;
import com.launchwindow.service.auth.LoginService;
import com.launchwindow.service.auth.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class})
class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RegistrationService registrationService;
    @MockitoBean
    private LoginService loginService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void anonymousUserCanLogIn() throws Exception {
        UserResponse user = new UserResponse(
                1L,
                "launch_test",
                "launch-test@example.com",
                Role.USER
        );
        LoginResponse response = new LoginResponse(
                "signed-token",
                "Bearer",
                7200,
                user
        );

        when(loginService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "launch_test",
                                  "password": "TestPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200))
                .andExpect(jsonPath("$.user.username").value("launch_test"));
    }

    @Test
    void invalidCredentialsReturnUnauthorized() throws Exception {
        when(loginService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "launch_test",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid login credentials"));
    }

    @Test
    void invalidLoginRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}