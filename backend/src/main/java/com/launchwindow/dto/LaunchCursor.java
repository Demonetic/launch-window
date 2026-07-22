package com.launchwindow.dto;

import java.time.Instant;

public record LaunchCursor(
        Instant afterTime,
        Long afterId,
        Short afterViewingScore
) {
    public LaunchCursor(Instant afterTime, Long afterId) {
        this(afterTime, afterId, null);
    }
}