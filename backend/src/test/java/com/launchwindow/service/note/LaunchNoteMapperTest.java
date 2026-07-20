package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LaunchNoteMapperTest {

    @Test
    void shouldMapPrivateLaunchNote() {
        LaunchNote note = mock(LaunchNote.class);
        Launch launch = mock(Launch.class);

        Instant createdAt = Instant.parse("2026-07-19T18:00:00Z");
        Instant updatedAt = Instant.parse("2026-07-19T18:30:00Z");

        when(note.getId()).thenReturn(10L);
        when(note.getLaunch()).thenReturn(launch);
        when(note.getContent()).thenReturn("Watch the webcast.");
        when(note.getCreatedAt()).thenReturn(createdAt);
        when(note.getUpdatedAt()).thenReturn(updatedAt);
        when(launch.getId()).thenReturn(4L);

        LaunchNoteResponse expected =
                new LaunchNoteResponse(
                        10L,
                        4L,
                        "Watch the webcast.",
                        createdAt,
                        updatedAt
                );

        assertEquals(expected, new LaunchNoteMapper().map(note));
    }
}