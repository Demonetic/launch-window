package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.RegisterRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.GlobalExceptionHandler;
import com.launchwindow.exception.UserAlreadyExistsException;
import com.launchwindow.model.Role;
import com.launchwindow.service.LoginService;
import com.launchwindow.service.RegistrationService;
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
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RegistrationService registrationService;
    @MockitoBean
    private LoginService loginService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void anonymousUserCanRegister() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "anna",
                "anna@example.com",
                Role.USER
        );

        when(registrationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "username": "anna",
                                  "email": "anna@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("anna"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void invalidRegistrationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "username": "a",
                                  "email": "invalid",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    @Test
    void duplicateUserReturnsConflict() throws Exception {
        when(registrationService.register(any(RegisterRequest.class))).thenThrow(new UserAlreadyExistsException("Email is already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anna",
                                  "email": "anna@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email is already in use"));
    }
}