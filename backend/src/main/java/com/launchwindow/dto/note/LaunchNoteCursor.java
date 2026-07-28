package com.launchwindow.dto.note;

import java.time.Instant;

public record LaunchNoteCursor(
        Instant beforeUpdatedAt,
        Long beforeId
) {
}