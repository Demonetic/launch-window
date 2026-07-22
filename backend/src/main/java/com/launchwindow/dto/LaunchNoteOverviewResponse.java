package com.launchwindow.dto;

import java.time.Instant;

public record LaunchNoteOverviewResponse(
        Long id,
        Long launchId,
        String launchName,
        Instant launchTime,
        String organizationName,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}