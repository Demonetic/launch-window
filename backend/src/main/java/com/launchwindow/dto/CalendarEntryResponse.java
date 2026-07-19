package com.launchwindow.dto;

import java.time.Instant;

public record CalendarEntryResponse(
        Long id,
        Instant savedAt,
        LaunchSummaryResponse launch
) {
}