package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryPadDto(
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        LaunchLibraryLocationDto location
) {
}
