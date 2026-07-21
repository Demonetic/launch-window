package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.CalendarPageResponse;
import com.launchwindow.dto.SavedLaunchIdsResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        CalendarPageResponse page = new CalendarPageResponse(List.of(),
                null, null, false, false);

        when(service.getInitialPage("launch_test", 20)).thenReturn(page);

        mockMvc.perform(get("/api/calendar").with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.previousCursor").doesNotExist())
                .andExpect(jsonPath("$.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(service).getInitialPage("launch_test", 20);
    }

    @Test
    void anonymousUserCannotGetCalendar() throws Exception {
        mockMvc.perform(get("/api/calendar"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/calendar"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void authenticatedUserCanCheckSavedLaunchIds() throws Exception {
        SavedLaunchIdsResponse response = new SavedLaunchIdsResponse(List.of(3L, 2L));

        when(service.getSavedLaunchIds("launch_test", List.of(3L, 1L, 2L))).thenReturn(response);

        mockMvc.perform(get("/api/calendar/saved-launch-ids")
                        .param("launchIds", "3,1,2")
                        .with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedLaunchIds").isArray())
                .andExpect(jsonPath("$.savedLaunchIds.length()").value(2))
                .andExpect(jsonPath("$.savedLaunchIds[0]").value(3))
                .andExpect(jsonPath("$.savedLaunchIds[1]").value(2));

        verify(service).getSavedLaunchIds("launch_test", List.of(3L, 1L, 2L));
    }
}