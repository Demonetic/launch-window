package com.launchwindow.service.weather;

import com.launchwindow.config.OpenMeteoProperties;
import com.launchwindow.integration.openmeteo.OpenMeteoClient;
import com.launchwindow.integration.openmeteo.WeatherForecastMapper;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.repository.WeatherSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.launchwindow.service.weather.WeatherTestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherSyncServiceTest {
    @Mock
    private OpenMeteoClient client;
    @Mock
    private WeatherForecastMapper mapper;
    @Mock
    private LaunchRepository launchRepository;
    @Mock
    private WeatherSnapshotRepository weatherRepository;

    private WeatherSyncService service;

    @BeforeEach
    void setUp() {
        service = new WeatherSyncService(client, mapper, launchRepository, weatherRepository,
                new OpenMeteoProperties("https://example.com", 16),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateUpdateAndSkipWeatherForecasts() {
        Launch createdLaunch = launch(1);
        Launch updatedLaunch = launch(2);
        Launch skippedLaunch = launch(3);

        when(createdLaunch.getId()).thenReturn(1L);
        when(updatedLaunch.getId()).thenReturn(2L);

        OpenMeteoResponse createdResponse = mock(OpenMeteoResponse.class);
        OpenMeteoResponse updatedResponse = mock(OpenMeteoResponse.class);
        OpenMeteoResponse skippedResponse = mock(OpenMeteoResponse.class);

        WeatherDetails createdDetails = details(NOW.plusSeconds(3600));
        WeatherDetails updatedDetails = details(NOW.plusSeconds(7200));
        WeatherSnapshot existingSnapshot = mock(WeatherSnapshot.class);

        when(launchRepository
                .findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
                        any(), any()
                ))
                .thenReturn(List.of(createdLaunch, updatedLaunch, skippedLaunch));

        when(client.fetchForecast(any(), any()))
                .thenReturn(createdResponse, updatedResponse, skippedResponse);

        when(mapper.map(createdResponse, createdLaunch.getLaunchTime(), NOW))
                .thenReturn(Optional.of(createdDetails));
        when(mapper.map(updatedResponse, updatedLaunch.getLaunchTime(), NOW))
                .thenReturn(Optional.of(updatedDetails));
        when(mapper.map(skippedResponse, skippedLaunch.getLaunchTime(), NOW))
                .thenReturn(Optional.empty());

        when(weatherRepository.findByLaunch_IdAndForecastTime(
                1L, createdDetails.forecastTime()
        )).thenReturn(Optional.empty());
        when(weatherRepository.findByLaunch_IdAndForecastTime(
                2L, updatedDetails.forecastTime()
        )).thenReturn(Optional.of(existingSnapshot));

        WeatherSyncResult result = service.syncUpcomingWeather();

        assertEquals(new WeatherSyncResult(3, 1, 1, 1), result);
        verify(weatherRepository).save(any(WeatherSnapshot.class));
        verify(existingSnapshot).update(updatedDetails);
    }
}