package com.launchwindow.service;

import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;

import java.time.Instant;

final class LaunchSyncTestData {

    static final Instant SYNC_TIME = Instant.parse("2026-07-18T14:00:00Z");

    private LaunchSyncTestData() {}

    static LaunchLibraryLaunchDto source(String id, String name) {
        return new LaunchLibraryLaunchDto(
                id, name, null, null, null,
                null, null, null, null, null
        );
    }

    static LaunchDetails details(String id, String name) {
        return new LaunchDetails(
                id,
                name,
                null,
                LaunchStatus.GO,
                Instant.parse("2026-08-01T10:00:00Z"),
                null,
                null,
                "Test rocket",
                null,
                null,
                null,
                null,
                null,
                null,
                SYNC_TIME
        );
    }
}