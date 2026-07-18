package com.launchwindow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "open-meteo.sync", name = "enabled", havingValue = "true")
public class WeatherSyncScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherSyncScheduler.class);

    private final WeatherSyncService service;

    public WeatherSyncScheduler(WeatherSyncService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${open-meteo.sync.interval}", initialDelayString = "${open-meteo.sync.initial-delay}")
    public void synchronize() {
        WeatherSyncResult result = service.syncUpcomingWeather();

        LOGGER.info(
                "Weather sync completed: processed={}, created={}, updated={}, skipped={}",
                result.processed(),
                result.created(),
                result.updated(),
                result.skipped()
        );
    }
}
