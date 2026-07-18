package com.launchwindow.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LaunchSyncSchedulerTest {
    @Test
    void synchronizeRunsLaunchSyncService() {
        LaunchSyncService service = mock(LaunchSyncService.class);
        LaunchSyncScheduler scheduler = new LaunchSyncScheduler(service);

        when(service.syncUpcomingLaunches()).thenReturn(new LaunchSyncResult(10, 2, 8));

        scheduler.synchronize();

        verify(service).syncUpcomingLaunches();
    }
}
