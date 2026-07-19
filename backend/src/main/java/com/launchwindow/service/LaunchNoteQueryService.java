package com.launchwindow.service;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LaunchNoteQueryService {
    private final AppUserRepository userRepository;
    private final LaunchNoteRepository noteRepository;
    private final LaunchNoteMapper mapper;

    public LaunchNoteQueryService(AppUserRepository userRepository, LaunchNoteRepository noteRepository,
                                  LaunchNoteMapper mapper) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<LaunchNoteResponse> getNotes(String username, Long launchId) {
        return userRepository.findByUsername(username)
                .map(user -> noteRepository.findAllByUser_IdAndLaunch_IdOrderByCreatedAtDesc(user.getId(), launchId)
                        .stream()
                        .map(mapper::map)
                        .toList())
                .orElseGet(List::of);
    }
}
