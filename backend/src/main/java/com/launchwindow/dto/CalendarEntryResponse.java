package com.launchwindow.dto;

import java.time.Instant;
import java.util.List;

public record CalendarEntryResponse(
        Long id,
        Instant savedAt,
        LaunchSummaryResponse launch,
        List<CalendarParticipantResponse> participants
) {
}