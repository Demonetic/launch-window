package com.launchwindow.dto.calendar;

import java.util.List;

public record CalendarPageResponse(
        List<CalendarEntryResponse> items,
        CalendarCursor previousCursor,
        CalendarCursor nextCursor,
        boolean hasPrevious,
        boolean hasNext
) {
}