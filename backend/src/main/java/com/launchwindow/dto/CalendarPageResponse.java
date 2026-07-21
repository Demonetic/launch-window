package com.launchwindow.dto;

import java.util.List;

public record CalendarPageResponse(
        List<CalendarEntryResponse> items,
        CalendarCursor previousCursor,
        CalendarCursor nextCursor,
        boolean hasPrevious,
        boolean hasNext
) {
}