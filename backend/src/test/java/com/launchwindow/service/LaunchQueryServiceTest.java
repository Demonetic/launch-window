package com.launchwindow.service;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static com.launchwindow.service.LaunchSyncTestData.SYNC_TIME;
import static com.launchwindow.service.LaunchSyncTestData.details;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LaunchQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-18T15:00:00Z");

    @Test
    void getUpcomingLaunches_returnsMappedSummaries() {
        LaunchRepository repository = mock(LaunchRepository.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        LaunchQueryService service = new LaunchQueryService(repository, clock);
        Launch launch = new Launch(details("launch-123", "Test launch"));

        when(repository.findAllByLaunchTimeAfterOrderByLaunchTimeAsc(CURRENT_TIME))
                .thenReturn(List.of(launch));

        List<LaunchSummaryResponse> result = service.getUpcomingLaunches();

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(
                        "Test launch",
                        result.getFirst().name()
                ),
                () -> assertEquals(
                        "Test rocket",
                        result.getFirst().rocketName()
                )
        );

        verify(repository).findAllByLaunchTimeAfterOrderByLaunchTimeAsc(CURRENT_TIME);
    }

    @Test
    void getLaunch_returnsMappedDetail_whenFound() {
        LaunchRepository repository = mock(LaunchRepository.class);
        Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
        LaunchQueryService service =
                new LaunchQueryService(repository, clock);

        Launch launch =
                new Launch(details("launch-123", "Test launch"));

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(launch));

        LaunchDetailResponse result =
                service.getLaunch(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Test launch", result.name()),
                () -> assertEquals("Test rocket", result.rocketName()),
                () -> assertEquals(
                        SYNC_TIME,
                        result.lastSyncedAt()
                )
        );
    }
}
