package com.launchwindow.service.launch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "launch-library.sync", name = "enabled", havingValue = "true")
public class LaunchSyncScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchSyncScheduler.class);
    private final LaunchSyncService service;

    public LaunchSyncScheduler(LaunchSyncService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${launch-library.sync.interval}",
            initialDelayString = "${launch-library.sync.initial-delay}")
    public void synchronize() {
        LaunchSyncResult result = service.syncUpcomingLaunches();

        LOGGER.info("Launch sync completed: processed={}, created={}, updated={}",
                result.processed(), result.created(), result.updated());
    }
}
