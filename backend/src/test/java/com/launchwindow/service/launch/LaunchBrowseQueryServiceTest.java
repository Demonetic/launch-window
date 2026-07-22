package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchBrowseFilter;
import com.launchwindow.dto.LaunchPageResponse;
import com.launchwindow.dto.LaunchSort;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaunchBrowseQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");

    private LaunchRepository repository;
    private WeatherSummaryQueryService weatherService;
    private LaunchQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(LaunchRepository.class);
        weatherService = mock(WeatherSummaryQueryService.class);

        service = new LaunchQueryService(repository, weatherService, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));
    }

    @Test
    void soonestBrowseAppliesFiltersAndCreatesTimeCursor() {
        Instant firstTime = Instant.parse("2026-07-23T10:00:00Z");
        Instant secondTime = Instant.parse("2026-07-24T10:00:00Z");

        Launch first = launch(1L, firstTime);
        Launch second = launch(2L, secondTime);
        Launch extra = mock(Launch.class);

        LaunchBrowseFilter filter =
                new LaunchBrowseFilter(LaunchSort.SOONEST, 30, Set.of(LaunchStatus.GO), "  SpaceX  ", true, (short) 60);

        when(repository.findBrowseSoonestPage(CURRENT_TIME, CURRENT_TIME.plusSeconds(30L * 24 * 60 * 60),
                true, Set.of(LaunchStatus.GO), "%spacex%", true, (short) 60,
                null, null, PageRequest.of(0, 3)))
                .thenReturn(List.of(first, second, extra));
        when(weatherService.getByLaunchIds(List.of(1L, 2L))).thenReturn(Map.of());

        LaunchPageResponse result =
                service.browseUpcomingLaunches(filter, null, null, null, 2);

        assertEquals(2, result.items().size());
        assertTrue(result.hasNext());
        assertNotNull(result.nextCursor());
        assertEquals(secondTime, result.nextCursor().afterTime());
        assertEquals(2L, result.nextCursor().afterId());
        assertNull(result.nextCursor().afterViewingScore());
    }

    @Test
    void bestViewingBrowseCreatesScoreCursor() {
        Instant firstTime = Instant.parse("2026-07-23T10:00:00Z");
        Instant secondTime = Instant.parse("2026-07-24T10:00:00Z");

        Launch first = launch(1L, firstTime);
        Launch second = launch(2L, secondTime);
        Launch extra = mock(Launch.class);

        WeatherSummaryResponse weather = mock(WeatherSummaryResponse.class);

        when(weather.viewingScore()).thenReturn((short) 75);

        LaunchBrowseFilter filter =
                new LaunchBrowseFilter(LaunchSort.BEST_VIEWING, null, Set.of(), null, null, null);

        when(repository.findBrowseBestViewingPage(CURRENT_TIME, null, false, EnumSet.allOf(LaunchStatus.class),
                null, null, null, null, null, null,
                PageRequest.of(0, 3))).thenReturn(List.of(first, second, extra));
        when(weatherService.getByLaunchIds(List.of(1L, 2L))).thenReturn(Map.of(2L, weather));

        LaunchPageResponse result =
                service.browseUpcomingLaunches(filter, null, null, null, 2);

        assertTrue(result.hasNext());
        assertEquals(secondTime, result.nextCursor().afterTime());
        assertEquals(2L, result.nextCursor().afterId());
        assertEquals((short) 75, result.nextCursor().afterViewingScore());
    }

    @Test
    void bestViewingUsesMinusOneForMissingWeather() {
        Instant launchTime = Instant.parse("2026-07-24T10:00:00Z");

        Launch launch = launch(4L, launchTime);
        Launch extra = mock(Launch.class);

        LaunchBrowseFilter filter =
                new LaunchBrowseFilter(LaunchSort.BEST_VIEWING, null, Set.of(), null, null, null);

        when(repository.findBrowseBestViewingPage(CURRENT_TIME, null, false, EnumSet.allOf(LaunchStatus.class),
                null, null, null, null, null, null,
                PageRequest.of(0, 2))).thenReturn(List.of(launch, extra));
        when(weatherService.getByLaunchIds(List.of(4L))).thenReturn(Map.of());

        LaunchPageResponse result =
                service.browseUpcomingLaunches(filter, null, null, null, 1);

        assertEquals((short) -1, result.nextCursor().afterViewingScore());
    }

    @Test
    void invalidBrowseFiltersAreRejected() {
        LaunchBrowseFilter invalidDays =
                new LaunchBrowseFilter(LaunchSort.SOONEST, 0, Set.of(), null, null, null);
        LaunchBrowseFilter invalidScore =
                new LaunchBrowseFilter(LaunchSort.SOONEST, null, Set.of(), null, null, (short) 101);
        LaunchBrowseFilter conflictingWeather =
                new LaunchBrowseFilter(LaunchSort.SOONEST, null, Set.of(), null, false, (short) 50);

        assertThrows(
                InvalidPaginationException.class,
                () -> service.browseUpcomingLaunches(invalidDays, null, null, null, 20));
        assertThrows(
                InvalidPaginationException.class,
                () -> service.browseUpcomingLaunches(invalidScore, null, null, null, 20));
        assertThrows(
                InvalidPaginationException.class,
                () -> service.browseUpcomingLaunches(conflictingWeather, null, null, null, 20)
        );

        verifyNoInteractions(repository, weatherService);
    }

    @Test
    void cursorMustMatchSelectedSort() {
        LaunchBrowseFilter soonest =
                new LaunchBrowseFilter(LaunchSort.SOONEST, null, Set.of(), null, null, null);
        LaunchBrowseFilter bestViewing =
                new LaunchBrowseFilter(LaunchSort.BEST_VIEWING, null, Set.of(), null, null, null);

        assertThrows(
                InvalidPaginationException.class,
                () -> service.browseUpcomingLaunches(soonest, CURRENT_TIME, 4L, (short) 80, 20));
        assertThrows(
                InvalidPaginationException.class,
                () -> service.browseUpcomingLaunches(bestViewing, CURRENT_TIME, 4L, null, 20));

        verifyNoInteractions(repository, weatherService);
    }

    private Launch launch(Long id, Instant launchTime) {
        Launch launch = mock(Launch.class);

        when(launch.getId()).thenReturn(id);
        when(launch.getLaunchTime()).thenReturn(launchTime);

        return launch;
    }
}