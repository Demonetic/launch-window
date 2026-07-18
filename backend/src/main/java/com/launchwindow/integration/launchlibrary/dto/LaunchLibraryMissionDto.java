package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryMissionDto(
        String type,
        String description
) {
}
