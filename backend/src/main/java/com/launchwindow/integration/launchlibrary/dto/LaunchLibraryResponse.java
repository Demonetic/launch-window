package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryResponse(
        int count,
        String next,
        String previous,
        List<LaunchLibraryLaunchDto> results
) {
}
