package com.launchwindow.service;

import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherDetails;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class WeatherTestData {
    static final Instant NOW =
            Instant.parse("2026-07-18T12:00:00Z");

    private WeatherTestData() {
    }

    static Launch launch(int hoursUntilLaunch) {
        Launch launch = mock(Launch.class);

        when(launch.getLatitude())
                .thenReturn(new BigDecimal("28.500000"));
        when(launch.getLongitude())
                .thenReturn(new BigDecimal("-80.600000"));
        when(launch.getLaunchTime())
                .thenReturn(NOW.plusSeconds(hoursUntilLaunch * 3600L));

        return launch;
    }

    static WeatherDetails details(Instant forecastTime) {
        return new WeatherDetails(
                forecastTime,
                new BigDecimal("20.00"),
                (short) 10,
                (short) 5,
                new BigDecimal("12.00"),
                20_000,
                (short) 90,
                NOW
        );
    }
}
