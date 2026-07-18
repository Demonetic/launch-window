package com.launchwindow.integration.openmeteo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoHourlyDto(
        List<Long> time,
        @JsonProperty("temperature_2m") List<BigDecimal> temperatures,
        @JsonProperty("cloud_cover") List<Integer> cloudCoverPercentages,
        @JsonProperty("precipitation_probability") List<Integer> precipitationProbabilities,
        @JsonProperty("wind_speed_10m") List<BigDecimal> windSpeeds,
        List<Integer> visibility
) {
}
