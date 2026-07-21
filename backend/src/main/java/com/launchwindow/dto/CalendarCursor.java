package com.launchwindow.dto;

import java.time.Instant;

public record CalendarCursor(
        Instant time,
        Long id
) {
}