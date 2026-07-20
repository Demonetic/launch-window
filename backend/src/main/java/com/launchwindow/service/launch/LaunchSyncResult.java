package com.launchwindow.service.launch;

public record LaunchSyncResult(
        int processed,
        int created,
        int updated
) {
}
