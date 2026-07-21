package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchLibraryClient;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.launchwindow.service.launch.LaunchSyncTestData.source;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LaunchSyncServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-21T12:00:00Z");

    private LaunchLibraryClient client;
    private LaunchSyncWriter writer;
    private LaunchSyncService service;

    @BeforeEach
    void setUp() {
        client = mock(LaunchLibraryClient.class);
        writer = mock(LaunchSyncWriter.class);

        service = new LaunchSyncService(client, writer, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));
    }

    @Test
    void synchronizeLaunches_fetchesAllDataBeforeWriting() {
        LaunchLibraryLaunchDto upcoming = source("launch-1", "Upcoming launch");
        LaunchLibraryLaunchDto recent = source("launch-2", "Recent launch");

        List<LaunchLibraryLaunchDto> merged = List.of(upcoming, recent);

        LaunchSyncResult expectedResult = new LaunchSyncResult(2, 1, 1);

        when(client.fetchUpcomingLaunches()).thenReturn(List.of(upcoming));
        when(client.fetchRecentLaunches(CURRENT_TIME.minus(48, ChronoUnit.HOURS), CURRENT_TIME))
                .thenReturn(List.of(recent));
        when(writer.synchronize(merged)).thenReturn(expectedResult);

        LaunchSyncResult result = service.synchronizeLaunches();

        assertSame(expectedResult, result);

        InOrder inOrder = inOrder(client, writer);

        inOrder.verify(client).fetchUpcomingLaunches();

        inOrder.verify(client).fetchRecentLaunches(CURRENT_TIME.minus(48, ChronoUnit.HOURS), CURRENT_TIME);

        inOrder.verify(writer).synchronize(merged);
    }

    @Test
    void synchronizeLaunches_recentVersionWinsForDuplicateId() {
        LaunchLibraryLaunchDto upcomingVersion = source("launch-1", "Upcoming version");
        LaunchLibraryLaunchDto recentVersion = source("launch-1", "Recent version");

        when(client.fetchUpcomingLaunches()).thenReturn(List.of(upcomingVersion));
        when(client.fetchRecentLaunches(CURRENT_TIME.minus(48, ChronoUnit.HOURS), CURRENT_TIME))
                .thenReturn(List.of(recentVersion));

        LaunchSyncResult expectedResult = new LaunchSyncResult(1, 0, 1);

        when(writer.synchronize(List.of(recentVersion))).thenReturn(expectedResult);

        LaunchSyncResult result = service.synchronizeLaunches();

        assertSame(expectedResult, result);

        verify(writer).synchronize(List.of(recentVersion));
    }

    @Test
    void synchronizeLaunches_doesNotWriteWhenUpcomingFetchFails() {
        when(client.fetchUpcomingLaunches())
                .thenThrow(new IllegalStateException("API unavailable"));

        assertThrows(IllegalStateException.class, service::synchronizeLaunches);

        verify(client, never()).fetchRecentLaunches(any(), any());
        verify(writer, never()).synchronize(anyList());
    }

    @Test
    void synchronizeLaunches_doesNotWriteWhenRecentFetchFails() {
        LaunchLibraryLaunchDto upcoming = source("launch-1", "Upcoming launch");

        when(client.fetchUpcomingLaunches()).thenReturn(List.of(upcoming));

        when(client.fetchRecentLaunches(CURRENT_TIME.minus(48, ChronoUnit.HOURS), CURRENT_TIME))
                .thenThrow(new IllegalStateException("API unavailable"));

        assertThrows(IllegalStateException.class, service::synchronizeLaunches);

        verify(writer, never()).synchronize(anyList());
    }
}