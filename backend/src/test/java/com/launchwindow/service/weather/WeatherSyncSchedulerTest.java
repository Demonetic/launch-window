package com.launchwindow.service.weather;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherSyncSchedulerTest {

    @Test
    void synchronizeRunsWeatherSyncService() {
        WeatherSyncService service = mock(WeatherSyncService.class);
        WeatherSyncScheduler scheduler = new WeatherSyncScheduler(service);

        when(service.syncUpcomingWeather())
                .thenReturn(new WeatherSyncResult(8, 2, 5, 1));

        scheduler.synchronize();

        verify(service).syncUpcomingWeather();
    }
}