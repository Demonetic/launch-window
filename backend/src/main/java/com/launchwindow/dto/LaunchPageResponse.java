package com.launchwindow.dto;

import java.util.List;

public record LaunchPageResponse(
        List<LaunchSummaryResponse> items,
        LaunchCursor nextCursor,
        boolean hasNext
) {
}
