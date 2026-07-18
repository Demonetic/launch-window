package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryStatusDto(
        int id,
        String name,
        String abbrev
) {
}
