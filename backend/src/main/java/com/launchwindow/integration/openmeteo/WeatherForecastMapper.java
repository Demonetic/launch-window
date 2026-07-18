package com.launchwindow.integration.openmeteo;

import com.launchwindow.integration.openmeteo.dto.OpenMeteoHourlyDto;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.service.ViewingScoreCalculator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class WeatherForecastMapper {
    private static final long MAX_DISTANCE_SECONDS = 3600;
    private final ViewingScoreCalculator scoreCalculator;

    public WeatherForecastMapper(ViewingScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    public Optional<WeatherDetails> map(OpenMeteoResponse response, Instant launchTime, Instant fetchedAt) {
        Objects.requireNonNull(response, "Weather response is required");
        Objects.requireNonNull(launchTime, "Launch time is required");
        Objects.requireNonNull(fetchedAt, "Fetch time is required");

        OpenMeteoHourlyDto hourly = response.hourly();
        if (hourly == null || hourly.time() == null) {
            return Optional.empty();
        }

        int index = findNearestIndex(hourly.time(), launchTime);
        if (index < 0 || !hasRequiredValues(hourly, index)) {
            return Optional.empty();
        }

        Instant forecastTime = Instant.ofEpochSecond(hourly.time().get(index));
        long distance = Math.abs(forecastTime.getEpochSecond() - launchTime.getEpochSecond());

        if (distance > MAX_DISTANCE_SECONDS) {
            return Optional.empty();
        }

        short cloudCover = toPercent(hourly.cloudCoverPercentages().get(index));
        short precipitation = toPercent(hourly.precipitationProbabilities().get(index));
        short viewingScore = scoreCalculator.calculate(cloudCover, precipitation,
                hourly.windSpeeds().get(index), visibilityAt(hourly, index));

        return Optional.of(new WeatherDetails(forecastTime, hourly.temperatures().get(index), cloudCover,
                precipitation, hourly.windSpeeds().get(index), visibilityAt(hourly, index), viewingScore, fetchedAt));
    }

    private int findNearestIndex(List<Long> times, Instant launchTime) {
        int nearestIndex = -1;
        long shortestDistance = Long.MAX_VALUE;

        for (int index = 0; index < times.size(); index++) {
            if (times.get(index) == null) {
                continue;
            }

            long distance = Math.abs(times.get(index) - launchTime.getEpochSecond());

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestIndex = index;
            }
        }

        return nearestIndex;
    }

    private boolean hasRequiredValues(OpenMeteoHourlyDto hourly, int index) {
        return hasValue(hourly.temperatures(), index)
                && hasValue(hourly.cloudCoverPercentages(), index)
                && hasValue(hourly.precipitationProbabilities(), index)
                && hasValue(hourly.windSpeeds(), index);
    }

    private boolean hasValue(List<?> values, int index) {
        return values != null
                && values.size() > index
                && values.get(index) != null;
    }

    private Integer visibilityAt(OpenMeteoHourlyDto hourly, int index) {
        return hasValue(hourly.visibility(), index)
                ? hourly.visibility().get(index)
                : null;
    }

    private short toPercent(int value) {
        return (short) Math.clamp(value, 0, 100);
    }
}
