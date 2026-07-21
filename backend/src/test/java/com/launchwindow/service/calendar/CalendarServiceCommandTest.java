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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CalendarServiceCommandTest {
    private AppUserRepository userRepository;
    private LaunchRepository launchRepository;
    private CalendarEntryRepository calendarRepository;
    private CalendarEntryMapper mapper;
    private CalendarService service;
    private WeatherSummaryQueryService weatherSummaryService;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        launchRepository = mock(LaunchRepository.class);
        calendarRepository = mock(CalendarEntryRepository.class);
        mapper = mock(CalendarEntryMapper.class);
        weatherSummaryService = mock(WeatherSummaryQueryService.class);

        service = new CalendarService(
                userRepository,
                launchRepository,
                calendarRepository,
                mapper,
                weatherSummaryService
        );
    }

    @Test
    void shouldSaveIdempotentlyAndRemoveLaunch() {
        AppUser user = mock(AppUser.class);
        Launch launch = mock(Launch.class);
        CalendarEntry entry = mock(CalendarEntry.class);
        CalendarEntryResponse response = mock(CalendarEntryResponse.class);
        WeatherSummaryResponse weather = mock(WeatherSummaryResponse.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(launchRepository.findById(4L)).thenReturn(Optional.of(launch));
        when(calendarRepository.findByUser_IdAndLaunch_Id(1L, 4L))
                .thenReturn(Optional.empty(), Optional.of(entry), Optional.of(entry));
        when(calendarRepository.save(any(CalendarEntry.class))).thenReturn(entry);
        when(weatherSummaryService.getByLaunchIds(List.of(4L))).thenReturn(Map.of(4L, weather));
        when(mapper.map(entry, weather)).thenReturn(response);

        Optional<CalendarEntryResponse> firstSave = service.saveLaunch("launch_test", 4L);
        Optional<CalendarEntryResponse> secondSave = service.saveLaunch("launch_test", 4L);
        boolean removed = service.removeLaunch("launch_test", 4L);

        assertAll(
                () -> assertEquals(Optional.of(response), firstSave),
                () -> assertEquals(Optional.of(response), secondSave),
                () -> assertTrue(removed)
        );

        verify(calendarRepository, times(1)).save(any(CalendarEntry.class));
        verify(calendarRepository).delete(entry);
        verify(weatherSummaryService, times(2)).getByLaunchIds(List.of(4L));
    }
}