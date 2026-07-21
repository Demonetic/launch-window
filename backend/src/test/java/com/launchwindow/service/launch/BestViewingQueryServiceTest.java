package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BestViewingQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-21T12:00:00Z");
    private LaunchRepository repository;
    private WeatherSummaryQueryService weatherSummaryService;
    private BestViewingQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(LaunchRepository.class);
        weatherSummaryService = mock(WeatherSummaryQueryService.class);

        service = new BestViewingQueryService(repository, weatherSummaryService, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));
    }

    @Test
    void getBestViewingLaunches_returnsRepositoryOrderWithWeather() {
        Launch bestLaunch = launch(1L, "Best launch", CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        Launch secondLaunch = launch(2L, "Second launch", CURRENT_TIME.plus(1, ChronoUnit.DAYS));

        WeatherSummaryResponse bestWeather = new WeatherSummaryResponse(
                (short) 90, ViewingCondition.EXCELLENT, CURRENT_TIME.plus(2, ChronoUnit.DAYS));

        WeatherSummaryResponse secondWeather = new WeatherSummaryResponse(
                (short) 75, ViewingCondition.GOOD, CURRENT_TIME.plus(1, ChronoUnit.DAYS));

        when(repository.findBestViewingLaunches(eq(CURRENT_TIME), eq(CURRENT_TIME.plus(7, ChronoUnit.DAYS)),
                any(Pageable.class))).thenReturn(List.of(bestLaunch, secondLaunch));

        when(weatherSummaryService.getByLaunchIds(List.of(1L, 2L))).thenReturn(Map.of(1L, bestWeather, 2L, secondWeather));

        List<LaunchSummaryResponse> result = service.getBestViewingLaunches(7, 3);

        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("Best launch", result.getFirst().name());
        assertSame(bestWeather, result.getFirst().weather());
        assertEquals(2L, result.getLast().id());
        assertEquals("Second launch", result.getLast().name());
        assertSame(secondWeather, result.getLast().weather());

        verify(weatherSummaryService).getByLaunchIds(List.of(1L, 2L));
    }

    @Test
    void getBestViewingLaunches_usesRequestedPeriodAndLimit() {
        when(repository.findBestViewingLaunches(any(), any(), any(Pageable.class))).thenReturn(List.of());

        service.getBestViewingLaunches(10, 5);

        verify(repository).findBestViewingLaunches(eq(CURRENT_TIME), eq(CURRENT_TIME.plus(10, ChronoUnit.DAYS)),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 5));
    }

    @Test
    void getBestViewingLaunches_returnsEmptyListWhenNoLaunchesMatch() {
        when(repository.findBestViewingLaunches(eq(CURRENT_TIME), eq(CURRENT_TIME.plus(7, ChronoUnit.DAYS)),
                any(Pageable.class))).thenReturn(List.of());

        List<LaunchSummaryResponse> result = service.getBestViewingLaunches(7, 3);

        assertTrue(result.isEmpty());

        verify(weatherSummaryService).getByLaunchIds(List.of());
    }

    @Test
    void getBestViewingLaunches_rejectsInvalidDays() {
        assertThrows(InvalidPaginationException.class, () -> service.getBestViewingLaunches(0, 3));
        assertThrows(InvalidPaginationException.class, () -> service.getBestViewingLaunches(17, 3));

        verifyNoInteractions(repository, weatherSummaryService);
    }

    @Test
    void getBestViewingLaunches_rejectsInvalidLimit() {
        assertThrows(InvalidPaginationException.class, () -> service.getBestViewingLaunches(7, 0));
        assertThrows(InvalidPaginationException.class, () -> service.getBestViewingLaunches(7, 11));

        verifyNoInteractions(repository, weatherSummaryService);
    }

    private Launch launch(Long id, String name, Instant launchTime) {
        Launch launch = mock(Launch.class);

        when(launch.getId()).thenReturn(id);
        when(launch.getName()).thenReturn(name);
        when(launch.getStatus()).thenReturn(LaunchStatus.GO);
        when(launch.getLaunchTime()).thenReturn(launchTime);
        when(launch.getImageUrl()).thenReturn("https://example.com/" + id + ".jpg");
        when(launch.getRocketName()).thenReturn("Test Rocket");
        when(launch.getOrganizationName()).thenReturn("Test Organization");
        when(launch.getPadName()).thenReturn("Test Pad");
        when(launch.getLocationName()).thenReturn("Test Location");

        return launch;
    }
}