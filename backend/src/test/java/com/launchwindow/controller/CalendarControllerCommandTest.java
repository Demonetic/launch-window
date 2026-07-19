package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.service.CalendarService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
@Import(SecurityConfiguration.class)
class CalendarControllerCommandTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanSaveLaunch() throws Exception {
        CalendarEntryResponse response = new CalendarEntryResponse(10L, null, null);

        when(service.saveLaunch("launch_test", 4L)).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/calendar/4").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void missingLaunchCannotBeSaved() throws Exception {
        when(service.saveLaunch("launch_test", 99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/calendar/99").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedUserCanRemoveLaunch() throws Exception {
        when(service.removeLaunch("launch_test", 4L)).thenReturn(true);

        mockMvc.perform(delete("/api/calendar/4").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void missingCalendarEntryCannotBeRemoved() throws Exception {
        when(service.removeLaunch("launch_test", 99L)).thenReturn(false);

        mockMvc.perform(delete("/api/calendar/99").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isNotFound());
    }
}