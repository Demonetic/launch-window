package com.launchwindow.model;

import java.math.BigDecimal;
import java.time.Instant;

public record WeatherDetails(
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
