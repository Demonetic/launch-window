package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.CalendarPageResponse;
import com.launchwindow.dto.CalendarParticipantResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
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
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CalendarServiceInitialPageTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-21T12:00:00Z");

    private AppUserRepository userRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarEntryMapper mapper;
    private WeatherSummaryQueryService weatherSummaryService;
    private CalendarParticipantQueryService participantService;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);
        mapper = mock(CalendarEntryMapper.class);
        weatherSummaryService = mock(WeatherSummaryQueryService.class);
        participantService = mock(CalendarParticipantQueryService.class);

        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);

        service = new CalendarService(userRepository, mock(LaunchRepository.class), calendarRepository, mapper,
                weatherSummaryService, participantService, clock);
    }

    @Test
    void getInitialPage_returnsFivePreviousAndFifteenNext() {
        AppUser user = user(1L);

        List<CalendarEntry> previousEntries = List.of(
                entry(5L, CURRENT_TIME.minus(1, ChronoUnit.HOURS)),
                entry(4L, CURRENT_TIME.minus(2, ChronoUnit.HOURS)),
                entry(3L, CURRENT_TIME.minus(3, ChronoUnit.HOURS)),
                entry(2L, CURRENT_TIME.minus(4, ChronoUnit.HOURS)),
                entry(1L, CURRENT_TIME.minus(5, ChronoUnit.HOURS))
        );
        List<CalendarEntry> nextEntries = createNextEntries(6L, 20L, CURRENT_TIME);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(previousEntries);
        when(calendarRepository.findNextInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(nextEntries);

        mockMappings(previousEntries, nextEntries);

        CalendarPageResponse result = service.getInitialPage("launch_test", 20);

        assertEquals(20, result.items().size());
        assertEquals(CURRENT_TIME.minus(5, ChronoUnit.HOURS), result.previousCursor().time());
        assertEquals(1L, result.previousCursor().id());
        assertEquals(CURRENT_TIME.plus(15, ChronoUnit.HOURS), result.nextCursor().time());
        assertEquals(20L, result.nextCursor().id());
        assertFalse(result.hasPrevious());
        assertFalse(result.hasNext());
    }

    @Test
    void getInitialPage_fillsMissingPreviousSlotsWithNextEntries() {
        AppUser user = user(1L);

        List<CalendarEntry> previousEntries = List.of(
                entry(2L, CURRENT_TIME.minus(1, ChronoUnit.HOURS)),
                entry(1L, CURRENT_TIME.minus(2, ChronoUnit.HOURS))
        );

        List<CalendarEntry> nextEntries = createNextEntries(3L, 22L, CURRENT_TIME);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(previousEntries);
        when(calendarRepository.findNextInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(nextEntries);

        mockMappings(previousEntries, nextEntries);

        CalendarPageResponse result = service.getInitialPage("launch_test", 20);

        assertEquals(20, result.items().size());
        assertEquals(1L, result.previousCursor().id());
        assertEquals(20L, result.nextCursor().id());
        assertFalse(result.hasPrevious());
        assertTrue(result.hasNext());
    }

    @Test
    void getInitialPage_fillsMissingNextSlotsWithPreviousEntries() {
        AppUser user = user(1L);

        List<CalendarEntry> previousEntries = createPreviousEntries(20L, 1L, CURRENT_TIME);
        List<CalendarEntry> nextEntries = List.of(
                entry(21L, CURRENT_TIME.plus(1, ChronoUnit.HOURS)),
                entry(22L, CURRENT_TIME.plus(2, ChronoUnit.HOURS))
        );

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(previousEntries);
        when(calendarRepository.findNextInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(nextEntries);

        mockMappings(previousEntries, nextEntries);

        CalendarPageResponse result = service.getInitialPage("launch_test", 20);

        assertEquals(20, result.items().size());
        assertEquals(3L, result.previousCursor().id());
        assertEquals(22L, result.nextCursor().id());
        assertTrue(result.hasPrevious());
        assertFalse(result.hasNext());
    }

    @Test
    void getInitialPage_returnsEntriesInChronologicalOrder() {
        AppUser user = user(1L);

        CalendarEntry latestPrevious = entry(2L, CURRENT_TIME.minus(1, ChronoUnit.HOURS));
        CalendarEntry earliestPrevious = entry(1L, CURRENT_TIME.minus(2, ChronoUnit.HOURS));
        CalendarEntry earliestNext = entry(3L, CURRENT_TIME.plus(1, ChronoUnit.HOURS));
        CalendarEntry latestNext = entry(4L, CURRENT_TIME.plus(2, ChronoUnit.HOURS));

        List<CalendarEntry> previousEntries = List.of(latestPrevious, earliestPrevious);
        List<CalendarEntry> nextEntries = List.of(earliestNext, latestNext);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(previousEntries);
        when(calendarRepository.findNextInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(nextEntries);

        Map<CalendarEntry, CalendarEntryResponse> responses = mockMappings(previousEntries, nextEntries);

        CalendarPageResponse result = service.getInitialPage("launch_test", 20);

        assertEquals(List.of(responses.get(earliestPrevious), responses.get(latestPrevious), responses.get(earliestNext),
                        responses.get(latestNext)), result.items());
    }

    @Test
    void getInitialPage_returnsEmptyPageWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        CalendarPageResponse result = service.getInitialPage("missing", 20);

        assertTrue(result.items().isEmpty());
        assertNull(result.previousCursor());
        assertNull(result.nextCursor());
        assertFalse(result.hasPrevious());
        assertFalse(result.hasNext());

        verifyNoInteractions(calendarRepository, mapper, weatherSummaryService);
    }

    @Test
    void getInitialPage_includesWeatherAndParticipants() {
        AppUser user = user(1L);

        CalendarEntry entry = entry(4L, CURRENT_TIME.plus(1, ChronoUnit.HOURS));
        WeatherSummaryResponse weather = mock(WeatherSummaryResponse.class);
        CalendarEntryResponse response = mock(CalendarEntryResponse.class);
        CalendarParticipantResponse participant = mock(CalendarParticipantResponse.class);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findPreviousInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(List.of());
        when(calendarRepository.findNextInitial(eq(1L), eq(CURRENT_TIME), any(Pageable.class))).thenReturn(List.of(entry));
        when(weatherSummaryService.getByLaunchIds(List.of(4L))).thenReturn(Map.of(4L, weather));
        when(participantService.getByLaunchIds(user, List.of(4L))).thenReturn(Map.of(4L, List.of(participant)));
        when(mapper.map(entry, weather, List.of(participant))).thenReturn(response);

        CalendarPageResponse result = service.getInitialPage("launch_test", 20);

        assertEquals(List.of(response), result.items());

        verify(weatherSummaryService).getByLaunchIds(List.of(4L));
        verify(mapper).map(entry, weather, List.of(participant));
    }

    @Test
    void getInitialPage_rejectsInvalidLimit() {
        assertThrows(InvalidPaginationException.class, () -> service.getInitialPage("launch_test", 0));

        assertThrows(InvalidPaginationException.class, () -> service.getInitialPage("launch_test", 101));

        verifyNoInteractions(userRepository, calendarRepository, mapper, weatherSummaryService);
    }

    private AppUser user(Long id) {
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private CalendarEntry entry(Long launchId, Instant launchTime) {
        Launch launch = mock(Launch.class);
        when(launch.getId()).thenReturn(launchId);
        when(launch.getLaunchTime()).thenReturn(launchTime);

        CalendarEntry entry = mock(CalendarEntry.class);
        when(entry.getLaunch()).thenReturn(launch);

        return entry;
    }

    private List<CalendarEntry> createNextEntries(long firstId, long lastId, Instant startingTime) {
        return LongStream.rangeClosed(firstId, lastId)
                .mapToObj(id -> {
                    long offset = id - firstId + 1;
                    return entry(id, startingTime.plus(offset, ChronoUnit.HOURS));
                })
                .toList();
    }

    private List<CalendarEntry> createPreviousEntries(long firstId, long lastId, Instant startingTime) {
        return LongStream
                .iterate(firstId, id -> id >= lastId, id -> id - 1)
                .mapToObj(id -> {
                    long offset = firstId - id + 1;
                    return entry(id, startingTime.minus(offset, ChronoUnit.HOURS));
                })
                .toList();
    }

    private Map<CalendarEntry, CalendarEntryResponse> mockMappings(List<CalendarEntry> previousEntries, List<CalendarEntry> nextEntries) {
        List<CalendarEntry> entries = Stream.concat(previousEntries.stream(), nextEntries.stream()).toList();

        List<Long> launchIds = entries.stream()
                .map(CalendarEntry::getLaunch)
                .map(Launch::getId)
                .toList();

        when(weatherSummaryService.getByLaunchIds(launchIds)).thenReturn(Map.of());
        when(participantService.getByLaunchIds(any(AppUser.class), eq(launchIds))).thenReturn(Map.of());

        Map<CalendarEntry, CalendarEntryResponse> responses = new LinkedHashMap<>();

        for (CalendarEntry entry : entries) {
            CalendarEntryResponse response = mock(CalendarEntryResponse.class);

            responses.put(entry, response);

            when(mapper.map(entry, null, List.of())).thenReturn(response);
        }

        return responses;
    }
}