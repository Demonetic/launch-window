package com.launchwindow.dto.note;

import java.util.List;

public record LaunchNotePageResponse(
        List<LaunchNoteOverviewResponse> items,
        LaunchNoteCursor nextCursor,
        boolean hasNext
) {
}