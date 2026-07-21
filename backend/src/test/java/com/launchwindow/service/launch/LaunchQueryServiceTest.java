package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
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
import com.launchwindow.dto.LaunchPageResponse;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.launchwindow.service.launch.LaunchSyncTestData.SYNC_TIME;
import static com.launchwindow.service.launch.LaunchSyncTestData.details;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaunchQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-18T15:00:00Z");

    @Test
    void getUpcomingLaunches_returnsPageWithNextCursor() {
        LaunchRepository repository = mock(LaunchRepository.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);

        Launch firstLaunch = launch(1L, "First launch");
        Launch secondLaunch = launch(2L, "Second launch");
        Launch extraLaunch = launch(3L, "Extra launch");

        WeatherSummaryResponse weather = new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT,
                        Instant.parse("2026-08-01T10:00:00Z"));

        when(repository.findUpcomingPage(CURRENT_TIME, null, null, PageRequest.of(0, 3)
        )).thenReturn(List.of(firstLaunch, secondLaunch, extraLaunch));

        when(weatherSummaryService.getByLaunchIds(List.of(1L, 2L))).thenReturn(Map.of(1L, weather));

        LaunchPageResponse result = service.getUpcomingLaunches(null, null, 2);

        assertAll(
                () -> assertEquals(2, result.items().size()),
                () -> assertEquals("First launch", result.items().get(0).name()),
                () -> assertEquals(weather, result.items().get(0).weather()),
                () -> assertNull(result.items().get(1).weather()),
                () -> assertTrue(result.hasNext()),
                () -> assertEquals(Instant.parse("2026-08-01T10:00:00Z"), result.nextCursor().afterTime()),
                () -> assertEquals(2L, result.nextCursor().afterId())
        );

        verify(weatherSummaryService).getByLaunchIds(List.of(1L, 2L));
    }

    @Test
    void getUpcomingLaunches_returnsNullWeatherWhenMissing() {
        LaunchRepository repository = mock(LaunchRepository.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);
        Launch launch = launch(1L, "Test launch");

        when(repository.findUpcomingPage(CURRENT_TIME, null, null, PageRequest.of(0, 21)))
                .thenReturn(List.of(launch));

        when(weatherSummaryService.getByLaunchIds(List.of(1L))).thenReturn(Map.of());

        LaunchPageResponse result = service.getUpcomingLaunches(null, null, 20);

        assertAll(
                () -> assertEquals(1, result.items().size()),
                () -> assertNull(result.items().getFirst().weather()),
                () -> assertFalse(result.hasNext()),
                () -> assertNull(result.nextCursor())
        );

        verify(repository).findUpcomingPage(CURRENT_TIME, null, null, PageRequest.of(0, 21));

        verify(weatherSummaryService).getByLaunchIds(List.of(1L));
    }

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

    @Test
    void getUpcomingLaunches_lastPageHasNoNextCursor() {
        LaunchRepository repository = mock(LaunchRepository.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, clock);

        Launch launch = launch(1L, "Only launch");

        when(repository.findUpcomingPage(CURRENT_TIME, null, null, PageRequest.of(0, 21)
        )).thenReturn(List.of(launch));

        when(weatherSummaryService.getByLaunchIds(List.of(1L))).thenReturn(Map.of());

        LaunchPageResponse result = service.getUpcomingLaunches(null, null, 20);

        assertAll(
                () -> assertEquals(1, result.items().size()),
                () -> assertFalse(result.hasNext()),
                () -> assertNull(result.nextCursor())
        );
    }

    @Test
    void getUpcomingLaunches_rejectsIncompleteCursor() {
        LaunchRepository repository = mock(LaunchRepository.class);

        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));

        assertThrows(InvalidPaginationException.class,
                () -> service.getUpcomingLaunches(Instant.parse("2026-08-01T10:00:00Z"), null, 20)
        );

        verifyNoInteractions(repository, weatherSummaryService);
    }

    @Test
    void getUpcomingLaunches_rejectsInvalidLimits() {
        LaunchRepository repository = mock(LaunchRepository.class);

        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        LaunchQueryService service = new LaunchQueryService(repository, weatherSummaryService, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));

        assertAll(
                () -> assertThrows(InvalidPaginationException.class,
                        () -> service.getUpcomingLaunches(null, null, 0)),
                () -> assertThrows(InvalidPaginationException.class,
                        () -> service.getUpcomingLaunches(null, null, 51))
        );

        verifyNoInteractions(repository, weatherSummaryService);
    }

    private Launch launch(Long id, String name) {
        Launch launch = spy(new Launch(details("launch-" + id, name)));

        when(launch.getId()).thenReturn(id);

        return launch;
    }
}
