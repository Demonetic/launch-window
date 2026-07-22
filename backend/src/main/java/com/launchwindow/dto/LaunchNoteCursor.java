package com.launchwindow.dto;

import java.time.Instant;

public record LaunchNoteCursor(
        Instant beforeUpdatedAt,
        Long beforeId
) {
}