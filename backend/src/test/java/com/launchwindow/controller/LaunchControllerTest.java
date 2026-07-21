package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LaunchCursor;
import com.launchwindow.dto.LaunchPageResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.service.launch.LaunchQueryService;
import com.launchwindow.service.launch.BestViewingQueryService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockitoBean
    private BestViewingQueryService bestViewingService;

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
        LaunchCursor cursor = new LaunchCursor(Instant.parse("2026-08-01T10:15:30Z"), 1L);
        LaunchPageResponse page = new LaunchPageResponse(List.of(launch), cursor, true);

        when(launchService.getUpcomingLaunches(null, null, 20)).thenReturn(page);

        mockMvc.perform(get("/api/launches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Artemis Test Launch"))
                .andExpect(jsonPath("$.items[0].status").value("GO"))
                .andExpect(jsonPath("$.items[0].rocketName").value("SLS Block 1"))
                .andExpect(jsonPath("$.items[0].weather.viewingScore").value(85))
                .andExpect(jsonPath("$.items[0].weather.viewingCondition").value("EXCELLENT"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor.afterTime").value("2026-08-01T10:15:30Z"))
                .andExpect(jsonPath("$.nextCursor.afterId").value(1));

        verify(launchService).getUpcomingLaunches(null, null, 20);
    }

    @Test
    void anonymousUserCanRequestNextLaunchBatch() throws Exception {
        Instant afterTime = Instant.parse("2026-08-01T10:15:30Z");

        LaunchPageResponse page = new LaunchPageResponse(List.of(), null, false);

        when(launchService.getUpcomingLaunches(afterTime, 42L, 10)).thenReturn(page);

        mockMvc.perform(get("/api/launches")
                        .param("afterTime", afterTime.toString())
                        .param("afterId", "42")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

        verify(launchService).getUpcomingLaunches(afterTime, 42L, 10);
    }

    @Test
    void incompleteCursorReturnsBadRequest() throws Exception {
        Instant afterTime = Instant.parse("2026-08-01T10:15:30Z");

        when(launchService.getUpcomingLaunches(afterTime, null, 20))
                .thenThrow(new InvalidPaginationException("afterTime and afterId must be provided together"));

        mockMvc.perform(get("/api/launches").param("afterTime", afterTime.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("afterTime and afterId must be provided together"))
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"))
                .andExpect(jsonPath("$.path").value("/api/launches"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void invalidCursorTimeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/launches")
                        .param("afterTime", "not-a-date")
                        .param("afterId", "42"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request could not be read"))
                .andExpect(jsonPath("$.path").value("/api/launches"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void anonymousUserCanGetBestViewingLaunches() throws Exception {
        WeatherSummaryResponse weather = new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT, Instant.parse("2026-08-01T10:00:00Z"));

        LaunchSummaryResponse launch = new LaunchSummaryResponse(
                1L,
                "Best launch",
                LaunchStatus.GO,
                Instant.parse("2026-08-01T10:15:30Z"),
                null,
                "Falcon 9",
                "SpaceX",
                "LC-39A",
                "Kennedy Space Center",
                weather
        );

        when(bestViewingService.getBestViewingLaunches(7, 3)).thenReturn(List.of(launch));

        mockMvc.perform(get("/api/launches/best-viewing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Best launch"))
                .andExpect(jsonPath("$[0].weather.viewingScore").value(85))
                .andExpect(jsonPath("$[0].weather.viewingCondition").value("EXCELLENT"));

        verify(bestViewingService).getBestViewingLaunches(7, 3);
    }

    @Test
    void bestViewingAcceptsCustomDaysAndLimit() throws Exception {
        when(bestViewingService.getBestViewingLaunches(10, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/launches/best-viewing")
                        .param("days", "10")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(bestViewingService).getBestViewingLaunches(10, 5);
    }

    @Test
    void invalidBestViewingLimitReturnsBadRequest() throws Exception {
        when(bestViewingService.getBestViewingLaunches(7, 11))
                .thenThrow(new InvalidPaginationException("limit must be between 1 and 10"));

        mockMvc.perform(get("/api/launches/best-viewing").param("limit", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("limit must be between 1 and 10"))
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"))
                .andExpect(jsonPath("$.path").value("/api/launches/best-viewing"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void frontendOriginCanMakePreflightRequest() throws Exception {
        mockMvc.perform(options("/api/launches")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void unknownOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/launches")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
