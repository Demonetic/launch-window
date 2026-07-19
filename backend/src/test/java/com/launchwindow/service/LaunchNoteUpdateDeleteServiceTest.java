package com.launchwindow.service;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaunchNoteUpdateDeleteServiceTest {
    private AppUserRepository userRepository;
    private LaunchNoteRepository noteRepository;
    private LaunchNoteMapper mapper;
    private LaunchNoteCommandService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        noteRepository = mock(LaunchNoteRepository.class);
        mapper = mock(LaunchNoteMapper.class);

        service = new LaunchNoteCommandService(
                userRepository,
                mock(LaunchRepository.class),
                noteRepository,
                mapper
        );
    }

    @Test
    void shouldUpdateAndDeleteOwnedNote() {
        AppUser user = mock(AppUser.class);
        LaunchNote note = mock(LaunchNote.class);
        LaunchNoteResponse response = mock(LaunchNoteResponse.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(
                        Optional.of(note),
                        Optional.of(note)
                );
        when(mapper.map(note)).thenReturn(response);

        Optional<LaunchNoteResponse> updated = service.updateNote(
                "launch_test", 10L, "  Updated note.  ");
        boolean deleted = service.deleteNote("launch_test", 10L);

        assertAll(
                () -> assertEquals(Optional.of(response), updated),
                () -> assertTrue(deleted)
        );

        verify(note).updateContent("Updated note.");
        verify(noteRepository).delete(note);
    }

    @Test
    void shouldRejectCommandsForUnownedOrMissingNote() {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUser_Id(99L, 1L)).thenReturn(Optional.empty());

        assertTrue(service.updateNote("launch_test", 99L, "Updated note").isEmpty());

        assertFalse(service.deleteNote("launch_test", 99L));

        verifyNoInteractions(mapper);
        verify(noteRepository, never()).delete(any(LaunchNote.class));
    }
}