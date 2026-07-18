package com.launchwindow.integration.launchlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaunchLibraryLaunchDto(
        String id,
        String name,
        LaunchLibraryStatusDto status,
        Instant net,
        LaunchLibraryImageDto image,
        LaunchLibraryRocketDto rocket,
        LaunchLibraryMissionDto mission,
        LaunchLibraryPadDto pad,
        @JsonProperty("launch_service_provider") LaunchLibraryAgencyDto launchServiceProvider,
        @JsonProperty("vid_urls") List<LaunchLibraryVideoDto> videoUrls
        ) {
}
