package com.launchwindow.service;

import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.WeatherSnapshotRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherQueryServiceTest {
    @Test
    void shouldReturnLatestWeatherForLaunch() {
        WeatherSnapshotRepository repository = mock(WeatherSnapshotRepository.class);
        WeatherSnapshot snapshot = mock(WeatherSnapshot.class);
        Launch launch = mock(Launch.class);
        WeatherQueryService service = new WeatherQueryService(repository);

        Instant forecastTime = Instant.parse("2026-07-23T14:00:00Z");
        Instant fetchedAt = Instant.parse("2026-07-19T00:00:00Z");

        when(repository.findFirstByLaunch_IdOrderByFetchedAtDesc(2L)).thenReturn(Optional.of(snapshot));
        when(snapshot.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(2L);
        when(snapshot.getForecastTime()).thenReturn(forecastTime);
        when(snapshot.getTemperatureC()).thenReturn(new BigDecimal("16.30"));
        when(snapshot.getCloudCoverPercent()).thenReturn((short) 0);
        when(snapshot.getPrecipitationProbabilityPercent()).thenReturn((short) 0);
        when(snapshot.getWindSpeedKmh()).thenReturn(new BigDecimal("14.50"));
        when(snapshot.getVisibilityMeters()).thenReturn(24_140);
        when(snapshot.getViewingScore()).thenReturn((short) 95);
        when(snapshot.getFetchedAt()).thenReturn(fetchedAt);

        WeatherResponse expected = new WeatherResponse(
                2L,
                forecastTime,
                new BigDecimal("16.30"),
                (short) 0,
                (short) 0,
                new BigDecimal("14.50"),
                24_140,
                (short) 95,
                fetchedAt
        );

        assertEquals(Optional.of(expected), service.getLatestWeather(2L));
    }

    @Test
    void shouldReturnEmptyWhenLaunchHasNoWeather() {
        WeatherSnapshotRepository repository = mock(WeatherSnapshotRepository.class);
        WeatherQueryService service = new WeatherQueryService(repository);

        when(repository.findFirstByLaunch_IdOrderByFetchedAtDesc(99L)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service.getLatestWeather(99L));
    }
}
