package com.launchwindow.dto;

import com.launchwindow.model.ViewingCondition;

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
        ViewingCondition viewingCondition,
        Instant fetchedAt
) {
}