package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.service.LaunchQueryService;
import com.launchwindow.service.WeatherQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaunchController.class)
@Import(SecurityConfiguration.class)
class WeatherControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaunchQueryService launchService;

    @MockitoBean
    private WeatherQueryService weatherService;

    @Test
    void anonymousUser_canGetLaunchWeather() throws Exception {
        WeatherResponse weather = new WeatherResponse(
                2L,
                Instant.parse("2026-07-23T14:00:00Z"),
                new BigDecimal("16.30"),
                (short) 0,
                (short) 0,
                new BigDecimal("14.50"),
                24_140,
                (short) 95,
                Instant.parse("2026-07-19T00:00:00Z")
        );

        when(weatherService.getLatestWeather(2L)).thenReturn(Optional.of(weather));

        mockMvc.perform(get("/api/launches/2/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.launchId").value(2))
                .andExpect(jsonPath("$.temperatureC").value(16.30))
                .andExpect(jsonPath("$.windSpeedKmh").value(14.50))
                .andExpect(jsonPath("$.visibilityMeters").value(24_140))
                .andExpect(jsonPath("$.viewingScore").value(95));
    }

    @Test
    void missingWeather_returnsNotFound() throws Exception {
        when(weatherService.getLatestWeather(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/launches/99/weather"))
                .andExpect(status().isNotFound());
    }
}
