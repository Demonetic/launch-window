package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CalendarServiceQueryTest {
    private AppUserRepository userRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarEntryMapper mapper;
    private CalendarService service;
    private WeatherSummaryQueryService weatherSummaryService;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);
        mapper = mock(CalendarEntryMapper.class);
        weatherSummaryService = mock(WeatherSummaryQueryService.class);

        service = new CalendarService(
                userRepository,
                mock(LaunchRepository.class),
                calendarRepository,
                mapper,
                weatherSummaryService
        );
    }

    @Test
    void shouldReturnAuthenticatedUsersCalendarWithWeather() {
        AppUser user = mock(AppUser.class);
        CalendarEntry entry = mock(CalendarEntry.class);
        Launch launch = mock(Launch.class);

        WeatherSummaryResponse weather = mock(WeatherSummaryResponse.class);

        CalendarEntryResponse response = mock(CalendarEntryResponse.class);

        when(user.getId()).thenReturn(1L);
        when(entry.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(4L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findAllByUser_IdOrderBySavedAtDesc(1L)).thenReturn(List.of(entry));
        when(weatherSummaryService.getByLaunchIds(List.of(4L))).thenReturn(Map.of(4L, weather));
        when(mapper.map(entry, weather)).thenReturn(response);

        assertEquals(List.of(response), service.getCalendar("launch_test"));

        verify(weatherSummaryService).getByLaunchIds(List.of(4L));

        verify(mapper).map(entry, weather);
    }

    @Test
    void shouldReturnCalendarEntryWithNullWeatherWhenMissing() {
        AppUser user = mock(AppUser.class);
        CalendarEntry entry = mock(CalendarEntry.class);
        Launch launch = mock(Launch.class);

        CalendarEntryResponse response = mock(CalendarEntryResponse.class);

        when(user.getId()).thenReturn(1L);
        when(entry.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(4L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(calendarRepository.findAllByUser_IdOrderBySavedAtDesc(1L)).thenReturn(List.of(entry));
        when(weatherSummaryService.getByLaunchIds(List.of(4L))).thenReturn(Map.of());
        when(mapper.map(entry, null)).thenReturn(response);

        assertEquals(List.of(response), service.getCalendar("launch_test"));

        verify(mapper).map(entry, null);
    }

    @Test
    void shouldReturnEmptyCalendarForMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(List.of(), service.getCalendar("missing"));

        verifyNoInteractions(calendarRepository, mapper, weatherSummaryService);
    }
}