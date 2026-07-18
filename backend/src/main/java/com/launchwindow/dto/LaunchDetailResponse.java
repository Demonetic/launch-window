package com.launchwindow.dto;

import com.launchwindow.model.LaunchStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record LaunchDetailResponse(
        Long id,
        String name,
        String description,
        LaunchStatus status,
        Instant launchTime,
        String imageUrl,
        String webcastUrl,
        String rocketName,
        String missionType,
        String organizationName,
        String padName,
        String locationName,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant lastSyncedAt
) {
}
