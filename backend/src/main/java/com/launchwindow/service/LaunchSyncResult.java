package com.launchwindow.service;

public record LaunchSyncResult(
        int processed,
        int created,
        int updated
) {
}
