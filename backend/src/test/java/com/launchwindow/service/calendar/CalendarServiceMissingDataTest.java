package com.launchwindow.service.calendar;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CalendarServiceMissingDataTest {

    @Test
    void shouldRejectCommandsForMissingUser() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        LaunchRepository launchRepository = mock(LaunchRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarEntryMapper mapper = mock(CalendarEntryMapper.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        CalendarService service = new CalendarService(
                userRepository,
                launchRepository,
                calendarRepository,
                mapper,
                weatherSummaryService
        );

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(launchRepository.findById(4L)).thenReturn(Optional.of(mock(Launch.class)));

        assertTrue(service.saveLaunch("missing", 4L).isEmpty());
        assertFalse(service.removeLaunch("missing", 4L));

        verifyNoInteractions(calendarRepository, mapper, weatherSummaryService);
    }

    @Test
    void shouldRejectSaveForMissingLaunch() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        LaunchRepository launchRepository = mock(LaunchRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarEntryMapper mapper = mock(CalendarEntryMapper.class);
        WeatherSummaryQueryService weatherSummaryService = mock(WeatherSummaryQueryService.class);

        CalendarService service = new CalendarService(
                userRepository,
                launchRepository,
                calendarRepository,
                mapper,
                weatherSummaryService
        );

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(mock(AppUser.class)));
        when(launchRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.saveLaunch("launch_test", 99L).isEmpty());

        verifyNoInteractions(calendarRepository, mapper, weatherSummaryService);
    }
}