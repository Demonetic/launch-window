package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteOverviewResponse;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;
import org.junit.jupiter.api.Test;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.AvatarKey;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LaunchNoteMapperTest {

    @Test
    void shouldMapPrivateLaunchNote() {
        LaunchNote note = mock(LaunchNote.class);
        Launch launch = mock(Launch.class);
        AppUser author = mock(AppUser.class);

        Instant createdAt = Instant.parse("2026-07-19T18:00:00Z");
        Instant updatedAt = Instant.parse("2026-07-19T18:30:00Z");

        when(note.getId()).thenReturn(10L);
        when(note.getLaunch()).thenReturn(launch);
        when(note.getUser()).thenReturn(author);
        when(note.getContent()).thenReturn("Watch the webcast.");
        when(note.getCreatedAt()).thenReturn(createdAt);
        when(note.getUpdatedAt()).thenReturn(updatedAt);
        when(launch.getId()).thenReturn(4L);
        when(author.getId()).thenReturn(1L);
        when(author.getUsername()).thenReturn("launch_test");
        when(author.getAvatarKey()).thenReturn(AvatarKey.ASTRONAUT);
        when(author.getAvatarColor()).thenReturn("#FFFFFF");

        LaunchNoteResponse expected =
                new LaunchNoteResponse(10L, 4L, 1L, "launch_test", AvatarKey.ASTRONAUT,
                        "#FFFFFF", "Watch the webcast.", createdAt, updatedAt);

        assertEquals(expected, new LaunchNoteMapper().map(note));
    }

    @Test
    void shouldMapOverviewFieldsInCorrectOrder() {
        LaunchNote note = mock(LaunchNote.class);
        Launch launch = mock(Launch.class);
        AppUser author = mock(AppUser.class);

        Instant launchTime = Instant.parse("2026-07-23T11:00:00Z");
        Instant createdAt = Instant.parse("2026-07-22T11:00:00Z");
        Instant updatedAt = Instant.parse("2026-07-22T11:30:00Z");

        when(note.getId()).thenReturn(5L);
        when(note.getLaunch()).thenReturn(launch);
        when(note.getUser()).thenReturn(author);
        when(note.getContent()).thenReturn("My shared note");
        when(note.getCreatedAt()).thenReturn(createdAt);
        when(note.getUpdatedAt()).thenReturn(updatedAt);
        when(launch.getId()).thenReturn(3L);
        when(launch.getName()).thenReturn("Test launch");
        when(launch.getLaunchTime()).thenReturn(launchTime);
        when(launch.getOrganizationName()).thenReturn("Test organization");
        when(launch.getImageUrl()).thenReturn("https://example.com/launch.jpg");
        when(author.getId()).thenReturn(1L);
        when(author.getUsername()).thenReturn("launch_test");
        when(author.getAvatarKey()).thenReturn(AvatarKey.ASTRONAUT);
        when(author.getAvatarColor()).thenReturn("#FFFFFF");

        LaunchNoteOverviewResponse expected =
                new LaunchNoteOverviewResponse(5L, 3L, "Test launch", launchTime, "Test organization",
                        "https://example.com/launch.jpg", 1L, "launch_test", AvatarKey.ASTRONAUT,
                        "#FFFFFF", "My shared note", createdAt, updatedAt);

        assertEquals(expected, new LaunchNoteMapper().mapOverview(note));
    }
}