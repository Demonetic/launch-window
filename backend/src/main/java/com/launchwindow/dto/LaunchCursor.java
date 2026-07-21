package com.launchwindow.dto;

import java.time.Instant;

public record LaunchCursor(
        Instant afterTime,
        Long afterId
) {
}
