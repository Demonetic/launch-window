package com.launchwindow.service.weather;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class ViewingScoreCalculator {
    private static final double CLOUD_WEIGHT = 0.45;
    private static final double PRECIPITATION_WEIGHT = 0.25;
    private static final double WIND_WEIGHT = 0.15;
    private static final double VISIBILITY_WEIGHT = 0.15;

    public short calculate(int cloudCoverPercent, int precipitationProbabilityPercent,
                           BigDecimal windSpeedKmh, Integer visibilityMeters) {
        Objects.requireNonNull(windSpeedKmh, "Wind speed is required");

        double cloudScore = 100 - clamp(cloudCoverPercent);
        double precipitationScore = 100 - clamp(precipitationProbabilityPercent);
        double windScore = clamp(100 - windSpeedKmh.doubleValue() * 2.5);
        double visibilityScore = calculateVisibilityScore(visibilityMeters);

        double total = cloudScore * CLOUD_WEIGHT
                + precipitationScore * PRECIPITATION_WEIGHT
                + windScore * WIND_WEIGHT
                + visibilityScore * VISIBILITY_WEIGHT;

        return (short) Math.round(clamp(total));
    }

    private double calculateVisibilityScore(Integer visibilityMeters) {
        if (visibilityMeters == null) {
            return 50;
        }

        return clamp(visibilityMeters / 200.0);
    }

    private double clamp(double value) {
        return Math.clamp(value, 0, 100);
    }
}
