package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.launchwindow.service.launch.LaunchSyncTestData.SYNC_TIME;
import static com.launchwindow.service.launch.LaunchSyncTestData.details;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaunchQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-18T15:00:00Z");

    @Test
    void getUpcomingLaunches_returnsMappedSummariesWithWeather() {
        LaunchRepository repository = mock(LaunchRepository.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);
        Launch launch = spy(new Launch(details("launch-123", "Test launch")));

        when(launch.getId()).thenReturn(1L);

        WeatherSummaryResponse weather = new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT,
                        Instant.parse("2026-08-01T10:00:00Z"));

        when(repository.findAllByLaunchTimeAfterOrderByLaunchTimeAsc(CURRENT_TIME)).thenReturn(List.of(launch));

        when(weatherSummaryService.getByLaunchIds(List.of(1L))).thenReturn(Map.of(1L, weather));

        List<LaunchSummaryResponse> result = service.getUpcomingLaunches();

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Test launch", result.getFirst().name()),
                () -> assertEquals("Test rocket", result.getFirst().rocketName()),
                () -> assertEquals(weather, result.getFirst().weather())
        );

        verify(repository).findAllByLaunchTimeAfterOrderByLaunchTimeAsc(CURRENT_TIME);
        verify(weatherSummaryService).getByLaunchIds(List.of(1L));
    }

    @Test
    void getUpcomingLaunches_returnsNullWeatherWhenMissing() {
        LaunchRepository repository = mock(LaunchRepository.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);
        Launch launch = spy(new Launch(details("launch-123", "Test launch")));

        when(launch.getId()).thenReturn(1L);
        when(repository.findAllByLaunchTimeAfterOrderByLaunchTimeAsc(CURRENT_TIME)).thenReturn(List.of(launch));
        when(weatherSummaryService.getByLaunchIds(List.of(1L))).thenReturn(Map.of());

        List<LaunchSummaryResponse> result = service.getUpcomingLaunches();

        assertNull(result.getFirst().weather());
    }

    @Test
    void getLaunch_returnsMappedDetail_whenFound() {
        LaunchRepository repository = mock(LaunchRepository.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);

        Launch launch =
                new Launch(details("launch-123", "Test launch"));

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(launch));

        LaunchDetailResponse result =
                service.getLaunch(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Test launch", result.name()),
                () -> assertEquals("Test rocket", result.rocketName()),
                () -> assertEquals(
                        SYNC_TIME,
                        result.lastSyncedAt()
                )
        );
    }
}
