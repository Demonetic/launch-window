package com.launchwindow.service;

public record WeatherSyncResult(
        int processed,
        int created,
        int updated,
        int skipped
) {
}
