package com.launchwindow.dto;

import java.util.List;

public record LaunchNotePageResponse(
        List<LaunchNoteOverviewResponse> items,
        LaunchNoteCursor nextCursor,
        boolean hasNext
) {
}