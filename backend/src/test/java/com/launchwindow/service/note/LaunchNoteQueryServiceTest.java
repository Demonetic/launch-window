package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LaunchNoteQueryServiceTest {
    private AppUserRepository userRepository;
    private LaunchNoteRepository noteRepository;
    private LaunchNoteMapper mapper;
    private LaunchNoteQueryService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        noteRepository = mock(LaunchNoteRepository.class);
        mapper = mock(LaunchNoteMapper.class);

        service = new LaunchNoteQueryService(
                userRepository,
                noteRepository,
                mapper
        );
    }

    @Test
    void shouldReturnAuthenticatedUsersLaunchNotes() {
        AppUser user = mock(AppUser.class);
        LaunchNote note = mock(LaunchNote.class);
        LaunchNoteResponse response = mock(LaunchNoteResponse.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findAllByUser_IdAndLaunch_IdOrderByCreatedAtDesc(1L, 4L))
                .thenReturn(List.of(note));
        when(mapper.map(note)).thenReturn(response);

        assertEquals(List.of(response), service.getNotes("launch_test", 4L));
    }

    @Test
    void shouldReturnEmptyListForMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(List.of(), service.getNotes("missing", 4L));

        verifyNoInteractions(noteRepository, mapper);
    }
}