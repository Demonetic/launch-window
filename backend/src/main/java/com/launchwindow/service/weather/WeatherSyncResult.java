package com.launchwindow.service.weather;

public record WeatherSyncResult(
        int processed,
        int created,
        int updated,
        int skipped
) {
}
