package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteCursor;
import com.launchwindow.dto.LaunchNoteOverviewResponse;
import com.launchwindow.dto.LaunchNotePageResponse;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class LaunchNoteQueryService {
    private static final int MAX_PAGE_SIZE = 100;

    private final AppUserRepository userRepository;
    private final LaunchNoteRepository noteRepository;
    private final LaunchNoteMapper mapper;

    public LaunchNoteQueryService(AppUserRepository userRepository, LaunchNoteRepository noteRepository, LaunchNoteMapper mapper) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<LaunchNoteResponse> getNotes(String username, Long launchId) {
        return userRepository.findByUsername(username)
                .map(user -> noteRepository
                        .findAllByUser_IdAndLaunch_IdOrderByCreatedAtDesc(user.getId(), launchId)
                        .stream()
                        .map(mapper::map)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public LaunchNotePageResponse getNotesPage(String username, Instant beforeUpdatedAt, Long beforeId, int limit) {
        validatePagination(beforeUpdatedAt, beforeId, limit);

        return userRepository.findByUsername(username)
                .map(user -> createPage(
                        beforeUpdatedAt == null
                                ? noteRepository.findOverviewInitial(user.getId(), PageRequest.of(0, limit + 1))
                                : noteRepository.findOverviewPage(user.getId(), beforeUpdatedAt, beforeId,
                                PageRequest.of(0, limit + 1)), limit))
                .orElseGet(this::emptyPage);
    }

    private LaunchNotePageResponse createPage(List<LaunchNote> fetchedNotes, int limit) {
        boolean hasNext = fetchedNotes.size() > limit;

        List<LaunchNote> notes = fetchedNotes.stream()
                .limit(limit)
                .toList();

        List<LaunchNoteOverviewResponse> items = notes.stream()
                .map(mapper::mapOverview)
                .toList();

        LaunchNoteCursor nextCursor = notes.isEmpty()
                ? null
                : cursorFrom(notes.getLast());

        return new LaunchNotePageResponse(items, nextCursor, hasNext);
    }

    private LaunchNoteCursor cursorFrom(LaunchNote note) {
        return new LaunchNoteCursor(note.getUpdatedAt(), note.getId());
    }

    private LaunchNotePageResponse emptyPage() {
        return new LaunchNotePageResponse(List.of(), null, false);
    }

    private void validatePagination(Instant beforeUpdatedAt, Long beforeId, int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("Limit must be between 1 and 100");
        }

        boolean hasTime = beforeUpdatedAt != null;
        boolean hasId = beforeId != null;

        if (hasTime != hasId || hasId && beforeId < 1) {
            throw new InvalidPaginationException("Note cursor time and id must both be provided");
        }
    }
}