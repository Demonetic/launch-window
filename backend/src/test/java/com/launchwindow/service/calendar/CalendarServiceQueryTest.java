package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CalendarServiceQueryTest {
    private AppUserRepository userRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarEntryMapper mapper;
    private CalendarService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);
        mapper = mock(CalendarEntryMapper.class);

        service = new CalendarService(
                userRepository,
                mock(LaunchRepository.class),
                calendarRepository,
                mapper
        );
    }

    @Test
    void shouldReturnAuthenticatedUsersCalendar() {
        AppUser user = mock(AppUser.class);
        CalendarEntry entry = mock(CalendarEntry.class);
        CalendarEntryResponse response = mock(CalendarEntryResponse.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findAllByUser_IdOrderBySavedAtDesc(1L))
                .thenReturn(List.of(entry));
        when(mapper.map(entry)).thenReturn(response);

        assertEquals(List.of(response), service.getCalendar("launch_test"));
    }

    @Test
    void shouldReturnEmptyCalendarForMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(List.of(), service.getCalendar("missing"));

        verifyNoInteractions(calendarRepository, mapper);
    }
}