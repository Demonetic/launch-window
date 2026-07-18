package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.service.LaunchQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaunchController.class)
@Import(SecurityConfiguration.class)
class LaunchDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaunchQueryService service;

    @Test
    void anonymousUserCanGetLaunchDetail() throws Exception {
        when(service.getLaunch(1L))
                .thenReturn(Optional.of(detail()));

        mockMvc.perform(get("/api/launches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Artemis Test Launch"))
                .andExpect(jsonPath("$.status").value("GO"))
                .andExpect(jsonPath("$.webcastUrl")
                        .value("https://example.com/webcast"));
    }

    @Test
    void missingLaunchReturnsNotFound() throws Exception {
        when(service.getLaunch(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/launches/999"))
                .andExpect(status().isNotFound());
    }

    private LaunchDetailResponse detail() {
        Instant launchTime =
                Instant.parse("2026-08-01T10:15:30Z");

        return new LaunchDetailResponse(
                1L,
                "Artemis Test Launch",
                "Test mission",
                LaunchStatus.GO,
                launchTime,
                "https://example.com/image.jpg",
                "https://example.com/webcast",
                "SLS Block 1",
                "Exploration",
                "NASA",
                "Launch Complex 39B",
                "Kennedy Space Center",
                null,
                null,
                Instant.parse("2026-07-18T15:00:00Z")
        );
    }
}
