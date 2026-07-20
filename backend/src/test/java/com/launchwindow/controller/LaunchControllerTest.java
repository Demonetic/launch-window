package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.service.launch.LaunchQueryService;
import com.launchwindow.service.weather.WeatherQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaunchController.class)
@Import(SecurityConfiguration.class)
class LaunchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private LaunchQueryService launchService;
    @MockitoBean
    private WeatherQueryService weatherService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void anonymousUserCanGetUpcomingLaunches() throws Exception {
        WeatherSummaryResponse weather = new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT,
                Instant.parse("2026-08-01T10:00:00Z"));
        LaunchSummaryResponse launch = new LaunchSummaryResponse(
                1L,
                "Artemis Test Launch",
                LaunchStatus.GO,
                Instant.parse("2026-08-01T10:15:30Z"),
                "https://example.com/image.jpg",
                "SLS Block 1",
                "NASA",
                "Launch Complex 39B",
                "Kennedy Space Center",
                weather
        );

        when(launchService.getUpcomingLaunches()).thenReturn(List.of(launch));

        mockMvc.perform(get("/api/launches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Artemis Test Launch"))
                .andExpect(jsonPath("$[0].status").value("GO"))
                .andExpect(jsonPath("$[0].rocketName").value("SLS Block 1"))
                .andExpect(jsonPath("$[0].rocketName").value("SLS Block 1"))
                .andExpect(jsonPath("$[0].weather.viewingScore").value(85))
                .andExpect(jsonPath("$[0].weather.viewingCondition").value("EXCELLENT"))
                .andExpect(jsonPath("$[0].weather.forecastTime").value("2026-08-01T10:00:00Z"));
    }
}
