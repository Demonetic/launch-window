package com.launchwindow.dto;

import com.launchwindow.model.ViewingCondition;

import java.time.Instant;

public record WeatherSummaryResponse(
        short viewingScore,
        ViewingCondition viewingCondition,
        Instant forecastTime
) {
}