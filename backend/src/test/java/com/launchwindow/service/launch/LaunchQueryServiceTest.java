package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static com.launchwindow.service.launch.LaunchSyncTestData.SYNC_TIME;
import static com.launchwindow.service.launch.LaunchSyncTestData.details;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaunchQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-18T15:00:00Z");

    @Test
    void getLaunch_returnsMappedDetail_whenFound() {
        LaunchRepository repository = mock(LaunchRepository.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);
        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);
        Launch launch = new Launch(details("launch-123", "Test launch"));

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(launch));

        LaunchDetailResponse result = service.getLaunch(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Test launch", result.name()),
                () -> assertEquals("Test rocket", result.rocketName()),
                () -> assertEquals(SYNC_TIME, result.lastSyncedAt())
        );
    }
}
