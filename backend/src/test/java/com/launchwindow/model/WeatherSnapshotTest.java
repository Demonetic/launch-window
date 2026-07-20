package com.launchwindow.model;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeatherSnapshotTest {
    @Test
    void shouldCreateSnapshotFromWeatherDetails() {
        Launch launch = mock(Launch.class);
        WeatherDetails details = createDetails(Instant.parse("2026-07-20T12:00:00Z"), new BigDecimal("12.50"),
                (short) 82, Instant.parse("2026-07-18T12:00:00Z"));

        WeatherSnapshot snapshot = new WeatherSnapshot(launch, details);

        assertAll(
                () -> assertEquals(launch, snapshot.getLaunch()),
                () -> assertEquals(details.forecastTime(), snapshot.getForecastTime()),
                () -> assertEquals(details.temperatureC(), snapshot.getTemperatureC()),
                () -> assertEquals(details.cloudCoverPercent(), snapshot.getCloudCoverPercent()),
                () -> assertEquals(details.viewingScore(), snapshot.getViewingScore()),
                () -> assertEquals(details.fetchedAt(), snapshot.getFetchedAt())
        );
    }

    @Test
    void shouldUpdateSnapshotFromNewWeatherDetails() {
        Launch launch = mock(Launch.class);

        WeatherDetails originalDetails = createDetails(Instant.parse("2026-07-20T12:00:00Z"),
                new BigDecimal("12.50"), (short) 82, Instant.parse("2026-07-18T12:00:00Z"));

        WeatherDetails updatedDetails = createDetails(Instant.parse("2026-07-20T15:00:00Z"),
                new BigDecimal("8.25"), (short) 45, Instant.parse("2026-07-19T12:00:00Z"));

        WeatherSnapshot snapshot = new WeatherSnapshot(launch, originalDetails);

        snapshot.update(updatedDetails);

        assertAll(
                () -> assertEquals(updatedDetails.forecastTime(), snapshot.getForecastTime()),
                () -> assertEquals(updatedDetails.temperatureC(), snapshot.getTemperatureC()),
                () -> assertEquals(updatedDetails.viewingScore(), snapshot.getViewingScore()),
                () -> assertEquals(updatedDetails.fetchedAt(), snapshot.getFetchedAt())
        );
    }

    private WeatherDetails createDetails(
            Instant forecastTime,
            BigDecimal temperature,
            short viewingScore,
            Instant fetchedAt
    ) {
        return new WeatherDetails(
                forecastTime,
                temperature,
                (short) 20,
                (short) 10,
                new BigDecimal("14.50"),
                20_000,
                viewingScore,
                fetchedAt
        );
    }
}
