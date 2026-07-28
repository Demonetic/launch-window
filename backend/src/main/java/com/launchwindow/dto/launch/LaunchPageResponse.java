package com.launchwindow.dto.launch;

import java.util.List;

public record LaunchPageResponse(
        List<LaunchSummaryResponse> items,
        LaunchCursor nextCursor,
        boolean hasNext
) {
}
