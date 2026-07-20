package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchLibraryClient;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchSyncService {
    private final LaunchLibraryClient client;
    private final LaunchSyncWriter writer;

    public LaunchSyncService(LaunchLibraryClient client, LaunchSyncWriter writer) {
        this.client = client;
        this.writer = writer;
    }

    public LaunchSyncResult syncUpcomingLaunches() {
        List<LaunchLibraryLaunchDto> launches = client.fetchUpcomingLaunches();

        return writer.synchronize(launches);
    }
}