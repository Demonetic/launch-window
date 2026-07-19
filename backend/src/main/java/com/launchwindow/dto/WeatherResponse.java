package com.launchwindow.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WeatherResponse(
        Long launchId,
        Instant forecastTime,
        BigDecimal temperatureC,
        short cloudCoverPercent,
        short precipitationProbabilityPercent,
        BigDecimal windSpeedKmh,
        Integer visibilityMeters,
        short viewingScore,
        Instant fetchedAt
) {
}
