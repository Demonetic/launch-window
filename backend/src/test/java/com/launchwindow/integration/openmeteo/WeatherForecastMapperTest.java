package com.launchwindow.integration.openmeteo;

import com.launchwindow.integration.openmeteo.dto.OpenMeteoHourlyDto;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.service.ViewingScoreCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherForecastMapperTest {
    private final WeatherForecastMapper mapper = new WeatherForecastMapper(new ViewingScoreCalculator());

    @Test
    void mapsForecastHourNearestToLaunchTime() {
        Instant forecastTime = Instant.parse("2026-08-01T10:00:00Z");
        Instant launchTime = Instant.parse("2026-08-01T10:20:00Z");
        Instant fetchedAt = Instant.parse("2026-07-18T19:30:00Z");

        OpenMeteoResponse response = response(List.of(forecastTime.minusSeconds(36000)
                .getEpochSecond(), forecastTime.getEpochSecond(), forecastTime.plusSeconds(3600)
                .getEpochSecond()));

        WeatherDetails result = mapper
                .map(response, launchTime, fetchedAt)
                .orElseThrow();

        assertAll(
                () -> assertEquals(forecastTime, result.forecastTime()),
                () -> assertEquals(new BigDecimal("24.5"), result.temperatureC()),
                () -> assertEquals(50, result.cloudCoverPercent()),
                () -> assertEquals(30, result.precipitationProbabilityPercent()),
                () -> assertEquals(new BigDecimal("20"), result.windSpeedKmh()),
                () -> assertEquals(10000, result.visibilityMeters()),
                () -> assertEquals(55, result.viewingScore()),
                () -> assertEquals(fetchedAt, result.fetchedAt())
        );
    }

    @Test
    void rejectsForecastTooFarFromLaunchTime() {
        Instant forecastTime = Instant.parse("2026-08-01T10:00:00Z");
        Instant launchTime = forecastTime.plusSeconds(7200);

        assertTrue(mapper.map(response(List.of(forecastTime.getEpochSecond())),
                launchTime, Instant.parse("2026-07-18T19:30:00Z")).isEmpty());
    }

    private OpenMeteoResponse response(List<Long> times) {
        int size = times.size();

        return new OpenMeteoResponse(new OpenMeteoHourlyDto(
                times,
                repeated(new BigDecimal("24.5"), size),
                repeated(50, size),
                repeated(30, size),
                repeated(new BigDecimal("20"), size),
                repeated(10000, size)
        ));
    }

    private <T> List<T> repeated(T value, int size) {
        return Collections.nCopies(size, value);
    }
}