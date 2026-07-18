package com.launchwindow.service;

import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(
        named = "RUN_LIVE_API_TEST",
        matches = "true"
)
class LaunchSyncLiveTest {

    @Autowired
    private LaunchSyncService service;

    @Autowired
    private LaunchRepository repository;

    @Test
    void syncsUpcomingLaunchesFromLiveApi() {
        long launchesBeforeSync = repository.count();

        LaunchSyncResult result = service.syncUpcomingLaunches();

        assertTrue(result.processed() > 0);
        assertEquals(
                result.processed(),
                result.created() + result.updated()
        );
        assertTrue(repository.count() >= launchesBeforeSync);
    }
}