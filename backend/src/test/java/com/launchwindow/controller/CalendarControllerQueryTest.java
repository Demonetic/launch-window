package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.service.calendar.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
@Import(SecurityConfiguration.class)
class CalendarControllerQueryTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanGetOwnCalendar() throws Exception {
        when(service.getCalendar("launch_test")).thenReturn(List.of());

        mockMvc.perform(get("/api/calendar").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).getCalendar("launch_test");
    }

    @Test
    void anonymousUserCannotGetCalendar() throws Exception {
        mockMvc.perform(get("/api/calendar")).andExpect(status().isUnauthorized());
    }
}