package com.launchwindow.dto.calendar;

import java.time.Instant;

public record CalendarCursor(
        Instant time,
        Long id
) {
}