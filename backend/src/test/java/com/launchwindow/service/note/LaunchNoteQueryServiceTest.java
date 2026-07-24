package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteOverviewResponse;
import com.launchwindow.dto.LaunchNotePageResponse;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        service = new LaunchNoteQueryService(userRepository, noteRepository, mapper);
    }

    @Test
    void shouldReturnAccessibleLaunchNotes() {
        AppUser user = mock(AppUser.class);
        LaunchNote note = mock(LaunchNote.class);
        LaunchNoteResponse response = mock(LaunchNoteResponse.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findAccessibleByLaunchId(1L, 4L, CalendarInvitationStatus.ACCEPTED)).thenReturn(List.of(note));
        when(mapper.map(note)).thenReturn(response);

        assertEquals(List.of(response), service.getNotes("launch_test", 4L));
    }

    @Test
    void initialPageReturnsLimitAndNextCursor() {
        AppUser user = mock(AppUser.class);
        LaunchNote firstNote = mock(LaunchNote.class);
        LaunchNote secondNote = mock(LaunchNote.class);
        LaunchNote extraNote = mock(LaunchNote.class);

        LaunchNoteOverviewResponse firstResponse = mock(LaunchNoteOverviewResponse.class);
        LaunchNoteOverviewResponse secondResponse = mock(LaunchNoteOverviewResponse.class);

        Instant secondUpdatedAt = Instant.parse("2026-07-22T10:00:00Z");

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findOverviewInitial(1L, CalendarInvitationStatus.ACCEPTED, PageRequest.of(0, 3)))
                .thenReturn(List.of(firstNote, secondNote, extraNote));
        when(mapper.mapOverview(firstNote)).thenReturn(firstResponse);
        when(mapper.mapOverview(secondNote)).thenReturn(secondResponse);
        when(secondNote.getUpdatedAt()).thenReturn(secondUpdatedAt);
        when(secondNote.getId()).thenReturn(12L);

        LaunchNotePageResponse result = service.getNotesPage("launch_test", null, null, 2);

        assertEquals(List.of(firstResponse, secondResponse), result.items());
        assertTrue(result.hasNext());
        assertNotNull(result.nextCursor());
        assertEquals(secondUpdatedAt, result.nextCursor().beforeUpdatedAt());
        assertEquals(12L, result.nextCursor().beforeId());

        verify(mapper, never()).mapOverview(extraNote);
    }

    @Test
    void cursorPageUsesCursorRepositoryQuery() {
        AppUser user = mock(AppUser.class);
        LaunchNote note = mock(LaunchNote.class);
        LaunchNoteOverviewResponse response = mock(LaunchNoteOverviewResponse.class);

        Instant beforeUpdatedAt = Instant.parse("2026-07-22T10:00:00Z");
        Instant noteUpdatedAt = Instant.parse("2026-07-21T18:00:00Z");

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));

        when(noteRepository.findOverviewPage(1L, CalendarInvitationStatus.ACCEPTED, beforeUpdatedAt, 20L,
                PageRequest.of(0, 3))).thenReturn(List.of(note));

        when(mapper.mapOverview(note)).thenReturn(response);
        when(note.getUpdatedAt()).thenReturn(noteUpdatedAt);
        when(note.getId()).thenReturn(14L);

        LaunchNotePageResponse result = service.getNotesPage("launch_test", beforeUpdatedAt, 20L, 2);

        assertEquals(List.of(response), result.items());
        assertFalse(result.hasNext());
        assertEquals(noteUpdatedAt, result.nextCursor().beforeUpdatedAt());
        assertEquals(14L, result.nextCursor().beforeId());

        verify(noteRepository).findOverviewPage(1L, CalendarInvitationStatus.ACCEPTED, beforeUpdatedAt, 20L,
                PageRequest.of(0, 3));
        verify(noteRepository, never()).findOverviewInitial(anyLong(), any(), any());
    }

    @Test
    void emptyNotesPageHasNoCursor() {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(noteRepository.findOverviewInitial(1L, CalendarInvitationStatus.ACCEPTED, PageRequest.of(0, 21))).thenReturn(List.of());

        LaunchNotePageResponse result = service.getNotesPage("launch_test", null, null, 20);

        assertTrue(result.items().isEmpty());
        assertNull(result.nextCursor());
        assertFalse(result.hasNext());

        verifyNoInteractions(mapper);
    }

    @Test
    void missingUserReturnsEmptyNotesPage() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        LaunchNotePageResponse result = service.getNotesPage("missing", null, null, 20);

        assertTrue(result.items().isEmpty());
        assertNull(result.nextCursor());
        assertFalse(result.hasNext());

        verifyNoInteractions(noteRepository, mapper);
    }

    @Test
    void partialCursorIsRejected() {
        Instant cursorTime = Instant.parse("2026-07-22T10:00:00Z");

        assertThrows(
                InvalidPaginationException.class,
                () -> service.getNotesPage("launch_test", cursorTime, null, 20)
        );

        assertThrows(
                InvalidPaginationException.class,
                () -> service.getNotesPage("launch_test", null, 10L, 20)
        );

        verifyNoInteractions(userRepository, noteRepository, mapper);
    }

    @Test
    void invalidPageLimitIsRejected() {
        assertThrows(
                InvalidPaginationException.class,
                () -> service.getNotesPage("launch_test", null, null, 0)
        );

        assertThrows(
                InvalidPaginationException.class,
                () -> service.getNotesPage("launch_test", null, null, 101)
        );

        verifyNoInteractions(userRepository, noteRepository, mapper
        );
    }

    @Test
    void shouldReturnEmptyListForMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(List.of(), service.getNotes("missing", 4L));

        verifyNoInteractions(noteRepository, mapper);
    }
}