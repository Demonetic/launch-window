package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchMapper;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.launchwindow.service.launch.LaunchSyncTestData.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LaunchSyncWriterTest {

    @Test
    void synchronize_createsNewLaunchAndUpdatesExistingLaunch() {
        LaunchMapper mapper = mock(LaunchMapper.class);
        LaunchRepository repository = mock(LaunchRepository.class);

        Clock clock = Clock.fixed(SYNC_TIME, ZoneOffset.UTC);

        LaunchSyncWriter writer = new LaunchSyncWriter(mapper, repository, clock);
        LaunchLibraryLaunchDto newSource = source("new-launch", "New launch");
        LaunchLibraryLaunchDto existingSource = source("existing-launch", "Updated launch");
        LaunchDetails newDetails = details("new-launch", "New launch");
        LaunchDetails updatedDetails = details("existing-launch", "Updated launch");
        Launch existingLaunch = new Launch(details("existing-launch", "Old launch"));

        when(mapper.map(newSource, SYNC_TIME)).thenReturn(newDetails);

        when(mapper.map(existingSource, SYNC_TIME)).thenReturn(updatedDetails);

        when(repository.findByExternalId("new-launch")).thenReturn(Optional.empty());

        when(repository.findByExternalId("existing-launch")).thenReturn(Optional.of(existingLaunch));

        LaunchSyncResult result = writer.synchronize(List.of(newSource, existingSource));

        ArgumentCaptor<Launch> captor = ArgumentCaptor.forClass(Launch.class);

        verify(repository).save(captor.capture());

        assertAll(
                () -> assertEquals(2, result.processed()),
                () -> assertEquals(1, result.created()),
                () -> assertEquals(1, result.updated()),
                () -> assertEquals("New launch", captor.getValue().getName()),
                () -> assertEquals("Updated launch", existingLaunch.getName())
        );
    }
}