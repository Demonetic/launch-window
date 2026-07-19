package com.launchwindow.service;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import com.launchwindow.repository.LaunchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LaunchNoteCreateServiceTest {
    private AppUserRepository userRepository;
    private LaunchRepository launchRepository;
    private LaunchNoteRepository noteRepository;
    private LaunchNoteMapper mapper;
    private LaunchNoteCommandService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        launchRepository = mock(LaunchRepository.class);
        noteRepository = mock(LaunchNoteRepository.class);
        mapper = mock(LaunchNoteMapper.class);

        service = new LaunchNoteCommandService(
                userRepository,
                launchRepository,
                noteRepository,
                mapper
        );
    }

    @Test
    void shouldCreateTrimmedNoteForAuthenticatedUser() {
        AppUser user = mock(AppUser.class);
        Launch launch = mock(Launch.class);
        LaunchNoteResponse response = mock(LaunchNoteResponse.class);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(launchRepository.findById(4L)).thenReturn(Optional.of(launch));
        when(noteRepository.save(any(LaunchNote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(any(LaunchNote.class))).thenReturn(response);

        Optional<LaunchNoteResponse> result =
                service.createNote(
                        "launch_test",
                        4L,
                        "  Watch the webcast.  "
                );

        ArgumentCaptor<LaunchNote> captor = ArgumentCaptor.forClass(LaunchNote.class);
        verify(noteRepository).save(captor.capture());

        LaunchNote savedNote = captor.getValue();

        assertEquals(Optional.of(response), result);
        assertEquals(user, savedNote.getUser());
        assertEquals(launch, savedNote.getLaunch());
        assertEquals("Watch the webcast.", savedNote.getContent());
    }

    @Test
    void shouldRejectCreateForMissingLaunch() {
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(mock(AppUser.class)));
        when(launchRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.createNote("launch_test", 99L, "Note").isEmpty());

        verifyNoInteractions(noteRepository, mapper);
    }
}