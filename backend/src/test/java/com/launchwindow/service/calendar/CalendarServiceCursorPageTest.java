package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.CalendarPageResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CalendarServiceCursorPageTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-21T12:00:00Z");

    private AppUserRepository userRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarEntryMapper mapper;
    private WeatherSummaryQueryService weatherSummaryService;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);
        mapper = mock(CalendarEntryMapper.class);
        weatherSummaryService = mock(WeatherSummaryQueryService.class);

        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);

        service = new CalendarService(userRepository, mock(LaunchRepository.class),
                calendarRepository, mapper, weatherSummaryService, clock);
    }

    @Test
    void getNextPage_usesAfterCursorAndReturnsNextCursor() {
        AppUser user = user(1L);
        Instant cursorTime = CURRENT_TIME.plus(1, ChronoUnit.HOURS);

        CalendarEntry first = entry(2L, CURRENT_TIME.plus(2, ChronoUnit.HOURS));
        CalendarEntry second = entry(3L, CURRENT_TIME.plus(3, ChronoUnit.HOURS));
        CalendarEntry extra = entry(4L, CURRENT_TIME.plus(4, ChronoUnit.HOURS));

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));

        when(calendarRepository.findNextPage(eq(1L), eq(cursorTime), eq(1L), any(Pageable.class))).
                thenReturn(List.of(first, second, extra));

        Map<CalendarEntry, CalendarEntryResponse> responses = mockMappings(List.of(first, second));

        CalendarPageResponse result = service.getNextPage("launch_test", cursorTime, 1L, 2);

        assertEquals(List.of(responses.get(first), responses.get(second)), result.items());

        assertFalse(result.hasPrevious());
        assertTrue(result.hasNext());

        assertEquals(CURRENT_TIME.plus(2, ChronoUnit.HOURS), result.previousCursor().time());
        assertEquals(2L, result.previousCursor().id());

        assertEquals(CURRENT_TIME.plus(3, ChronoUnit.HOURS), result.nextCursor().time());
        assertEquals(3L, result.nextCursor().id());

        verify(calendarRepository).findNextPage(eq(1L), eq(cursorTime), eq(1L), argThat(pageable -> pageable.getPageSize() == 3));
    }

    @Test
    void getPreviousPage_returnsEntriesInChronologicalOrder() {
        AppUser user = user(1L);
        Instant cursorTime = CURRENT_TIME.minus(1, ChronoUnit.HOURS);

        CalendarEntry newest = entry(3L, CURRENT_TIME.minus(2, ChronoUnit.HOURS));
        CalendarEntry oldest = entry(2L, CURRENT_TIME.minus(3, ChronoUnit.HOURS));
        CalendarEntry extra = entry(1L, CURRENT_TIME.minus(4, ChronoUnit.HOURS));

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousPage(eq(1L), eq(cursorTime), eq(4L), any(Pageable.class)))
                .thenReturn(List.of(newest, oldest, extra));

        Map<CalendarEntry, CalendarEntryResponse> responses = mockMappings(List.of(oldest, newest));

        CalendarPageResponse result = service.getPreviousPage("launch_test", cursorTime, 4L, 2);

        assertEquals(List.of(responses.get(oldest), responses.get(newest)), result.items());
        assertTrue(result.hasPrevious());
        assertFalse(result.hasNext());
        assertEquals(CURRENT_TIME.minus(3, ChronoUnit.HOURS), result.previousCursor().time());
        assertEquals(2L, result.previousCursor().id());
        assertEquals(CURRENT_TIME.minus(2, ChronoUnit.HOURS), result.nextCursor().time());
        assertEquals(3L, result.nextCursor().id());
    }

    @Test
    void getNextPage_returnsNullWeatherWhenMissing() {
        AppUser user = user(1L);

        CalendarEntry entry = entry(4L, CURRENT_TIME.plus(1, ChronoUnit.HOURS));
        CalendarEntryResponse response = mock(CalendarEntryResponse.class);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findNextPage(eq(1L), eq(CURRENT_TIME), eq(3L), any(Pageable.class)))
                .thenReturn(List.of(entry));
        when(weatherSummaryService.getByLaunchIds(List.of(4L))).thenReturn(Map.of());
        when(mapper.map(entry, null)).thenReturn(response);

        CalendarPageResponse result = service.getNextPage("launch_test", CURRENT_TIME, 3L, 20);

        assertEquals(List.of(response), result.items());
        assertFalse(result.hasNext());

        verify(mapper).map(entry, null);
    }

    @Test
    void getNextPage_returnsEmptyPageForMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        CalendarPageResponse result = service.getNextPage("missing", CURRENT_TIME, 3L, 20);

        assertTrue(result.items().isEmpty());
        assertNull(result.previousCursor());
        assertNull(result.nextCursor());
        assertFalse(result.hasPrevious());
        assertFalse(result.hasNext());

        verifyNoInteractions(calendarRepository, mapper, weatherSummaryService);
    }

    @Test
    void getNextPage_rejectsIncompleteCursor() {
        assertThrows(InvalidPaginationException.class, () -> service.getNextPage(
                        "launch_test",
                        null,
                        1L,
                        20
                )
        );
        assertThrows(InvalidPaginationException.class, () -> service.getNextPage(
                        "launch_test",
                        CURRENT_TIME,
                        null,
                        20
                )
        );
        assertThrows(InvalidPaginationException.class, () -> service.getNextPage(
                        "launch_test",
                        CURRENT_TIME,
                        0L,
                        20
                )
        );

        verifyNoInteractions(userRepository, calendarRepository, mapper, weatherSummaryService);
    }

    @Test
    void getPreviousPage_rejectsInvalidLimit() {
        assertThrows(InvalidPaginationException.class, () -> service.getPreviousPage(
                        "launch_test",
                        CURRENT_TIME,
                        3L,
                        0
                )
        );
        assertThrows(InvalidPaginationException.class, () -> service.getPreviousPage(
                        "launch_test",
                        CURRENT_TIME,
                        3L,
                        101
                )
        );

        verifyNoInteractions(userRepository, calendarRepository, mapper, weatherSummaryService);
    }

    private AppUser user(Long id) {AppUser user = mock(AppUser.class);when(user.getId()).thenReturn(id);return user;}

    private CalendarEntry entry(Long launchId, Instant launchTime) {
        Launch launch = mock(Launch.class);
        when(launch.getId()).thenReturn(launchId);
        when(launch.getLaunchTime()).thenReturn(launchTime);

        CalendarEntry entry = mock(CalendarEntry.class);
        when(entry.getLaunch()).thenReturn(launch);

        return entry;
    }

    private Map<CalendarEntry, CalendarEntryResponse> mockMappings(List<CalendarEntry> entries) {
        List<Long> launchIds = entries.stream()
                .map(CalendarEntry::getLaunch)
                .map(Launch::getId)
                .toList();

        when(weatherSummaryService.getByLaunchIds(launchIds)).thenReturn(Map.of());

        Map<CalendarEntry, CalendarEntryResponse> responses = new LinkedHashMap<>();

        for (CalendarEntry entry : entries) {
            CalendarEntryResponse response = mock(CalendarEntryResponse.class);

            responses.put(entry, response);

            when(mapper.map(entry, null)).thenReturn(response);
        }

        return responses;
    }
}