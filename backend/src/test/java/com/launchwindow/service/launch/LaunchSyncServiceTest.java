package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchLibraryClient;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static com.launchwindow.service.launch.LaunchSyncTestData.source;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LaunchSyncServiceTest {

    @Test
    void syncUpcomingLaunches_fetchesBeforeWriting() {
        LaunchLibraryClient client = mock(LaunchLibraryClient.class);
        LaunchSyncWriter writer = mock(LaunchSyncWriter.class);
        LaunchSyncService service = new LaunchSyncService(client, writer);

        List<LaunchLibraryLaunchDto> launches = List.of(source("launch-1", "First launch"),
                source("launch-2", "Second launch"));

        LaunchSyncResult expectedResult = new LaunchSyncResult(2, 1, 1);

        when(client.fetchUpcomingLaunches()).thenReturn(launches);

        when(writer.synchronize(launches)).thenReturn(expectedResult);

        LaunchSyncResult result = service.syncUpcomingLaunches();

        assertSame(expectedResult, result);
        verify(client).fetchUpcomingLaunches();
        verify(writer).synchronize(launches);
    }

    @Test
    void syncUpcomingLaunches_doesNotWriteWhenFetchingFails() {
        LaunchLibraryClient client = mock(LaunchLibraryClient.class);
        LaunchSyncWriter writer = mock(LaunchSyncWriter.class);
        LaunchSyncService service = new LaunchSyncService(client, writer);

        when(client.fetchUpcomingLaunches()).thenThrow(new IllegalStateException("API unavailable"));

        assertThrows(IllegalStateException.class, service::syncUpcomingLaunches);

        verify(writer, never()).synchronize(anyList());
    }
}