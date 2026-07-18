package com.launchwindow.integration.openmeteo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(
        OpenMeteoHourlyDto hourly
) {
}
