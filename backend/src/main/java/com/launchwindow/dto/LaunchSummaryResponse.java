package com.launchwindow.dto;

import com.launchwindow.model.LaunchStatus;

import java.time.Instant;

public record LaunchSummaryResponse(
        Long id,
        String name,
        LaunchStatus status,
        Instant launchTime,
        String imageUrl,
        String rocketName,
        String organizationName,
        String padName,
        String locationName
) {
}
