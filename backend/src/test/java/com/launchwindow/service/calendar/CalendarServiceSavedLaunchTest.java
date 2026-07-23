package com.launchwindow.service.calendar;

import com.launchwindow.dto.SavedLaunchIdsResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CalendarServiceSavedLaunchTest {
    private AppUserRepository userRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);

        service = new CalendarService(
                userRepository,
                mock(LaunchRepository.class),
                calendarRepository,
                mock(CalendarEntryMapper.class),
                mock(WeatherSummaryQueryService.class),
                mock(CalendarParticipantQueryService.class),
                Clock.fixed(Instant.parse("2026-07-22T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void getSavedLaunchIdsReturnsSavedIdsInRequestOrder() {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findSavedLaunchIds(1L, List.of(3L, 1L, 2L))).thenReturn(List.of(1L, 3L));

        SavedLaunchIdsResponse result = service.getSavedLaunchIds("launch_test", List.of(3L, 1L, 3L, 2L));

        assertEquals(List.of(3L, 1L), result.savedLaunchIds());

        verify(calendarRepository).findSavedLaunchIds(1L, List.of(3L, 1L, 2L));
    }

    @Test
    void getSavedLaunchIdsReturnsEmptyResponseWhenUserIsMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        SavedLaunchIdsResponse result = service.getSavedLaunchIds("missing", List.of(1L, 2L));

        assertEquals(List.of(), result.savedLaunchIds());
        verifyNoInteractions(calendarRepository);
    }

    @Test
    void getSavedLaunchIdsRejectsInvalidSelections() {
        List<Long> tooManyIds = java.util.stream.LongStream
                .rangeClosed(1, 51)
                .boxed()
                .toList();

        assertThrows(InvalidPaginationException.class, () -> service.getSavedLaunchIds("launch_test", List.of()));
        assertThrows(InvalidPaginationException.class, () -> service.getSavedLaunchIds("launch_test", List.of(0L)));
        assertThrows(InvalidPaginationException.class, () -> service.getSavedLaunchIds("launch_test", tooManyIds));

        verifyNoInteractions(userRepository, calendarRepository);
    }
}