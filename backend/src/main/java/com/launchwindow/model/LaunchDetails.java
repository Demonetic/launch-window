package com.launchwindow.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LaunchDetails(
        String externalId,
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
        String countryCode,
        String countryName,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant lastSyncedAt
) {
    public LaunchDetails(
            String externalId,
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
        this(
                externalId,
                name,
                description,
                status,
                launchTime,
                imageUrl,
                webcastUrl,
                rocketName,
                missionType,
                organizationName,
                padName,
                locationName,
                null,
                null,
                latitude,
                longitude,
                lastSyncedAt
        );
    }
}
