package com.launchwindow.dto.calendar;

import com.launchwindow.dto.launch.LaunchSummaryResponse;

import java.time.Instant;
import java.util.List;

public record CalendarEntryResponse(
        Long id,
        Instant savedAt,
        LaunchSummaryResponse launch,
        List<CalendarParticipantResponse> participants
) {
}