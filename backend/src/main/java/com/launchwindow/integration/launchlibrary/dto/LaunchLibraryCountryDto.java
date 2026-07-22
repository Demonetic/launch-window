package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryCountryDto(
        @JsonProperty("alpha_3_code") String alpha3Code
) {
}