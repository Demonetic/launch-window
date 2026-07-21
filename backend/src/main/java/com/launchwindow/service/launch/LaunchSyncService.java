package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchLibraryClient;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LaunchSyncService {
    private static final int RECENT_WINDOW_HOURS = 48;

    private final LaunchLibraryClient client;
    private final LaunchSyncWriter writer;
    private final Clock clock;

    public LaunchSyncService(LaunchLibraryClient client, LaunchSyncWriter writer, Clock clock) {
        this.client = client;
        this.writer = writer;
        this.clock = clock;
    }

    public LaunchSyncResult synchronizeLaunches() {
        Instant now = clock.instant();
        Instant recentStart = now.minus(RECENT_WINDOW_HOURS, ChronoUnit.HOURS);

        List<LaunchLibraryLaunchDto> upcoming = client.fetchUpcomingLaunches();
        List<LaunchLibraryLaunchDto> recent = client.fetchRecentLaunches(recentStart, now);

        return writer.synchronize(merge(upcoming, recent));
    }

    private List<LaunchLibraryLaunchDto> merge(List<LaunchLibraryLaunchDto> upcoming, List<LaunchLibraryLaunchDto> recent) {
        Map<String, LaunchLibraryLaunchDto> launchesById = new LinkedHashMap<>();

        upcoming.forEach(launch -> launchesById.put(launch.id(), launch));

        recent.forEach(launch -> launchesById.put(launch.id(), launch));

        return List.copyOf(launchesById.values());
    }
}