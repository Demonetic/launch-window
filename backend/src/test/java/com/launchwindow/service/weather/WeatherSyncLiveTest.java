package com.launchwindow.service.weather;

import com.launchwindow.repository.WeatherSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(
        named = "RUN_LIVE_WEATHER_TEST",
        matches = "true"
)
class WeatherSyncLiveTest {
    @Autowired
    private WeatherSyncService service;

    @Autowired
    private WeatherSnapshotRepository repository;

    @Test
    void syncsWeatherFromLiveApi() {
        long snapshotsBeforeSync = repository.count();

        WeatherSyncResult result = service.syncUpcomingWeather();

        assertTrue(result.processed() > 0);
        assertEquals(result.processed(), result.created() + result.updated() + result.skipped());
        assertEquals(snapshotsBeforeSync + result.created(), repository.count());
    }
}