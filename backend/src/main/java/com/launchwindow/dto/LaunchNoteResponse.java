package com.launchwindow.dto;

import java.time.Instant;

public record LaunchNoteResponse(
        Long id,
        Long launchId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
