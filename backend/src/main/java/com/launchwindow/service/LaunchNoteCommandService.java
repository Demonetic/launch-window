package com.launchwindow.service;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import com.launchwindow.repository.LaunchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LaunchNoteCommandService {
    private final AppUserRepository userRepository;
    private final LaunchRepository launchRepository;
    private final LaunchNoteRepository noteRepository;
    private final LaunchNoteMapper mapper;

    public LaunchNoteCommandService(AppUserRepository userRepository, LaunchRepository launchRepository,
            LaunchNoteRepository noteRepository, LaunchNoteMapper mapper) {
        this.userRepository = userRepository;
        this.launchRepository = launchRepository;
        this.noteRepository = noteRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Optional<LaunchNoteResponse> createNote(String username, Long launchId, String content) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        Optional<Launch> launch = launchRepository.findById(launchId);

        if (user.isEmpty() || launch.isEmpty()) {
            return Optional.empty();
        }

        LaunchNote note = noteRepository.save(new LaunchNote(
                user.get(),
                launch.get(),
                content.trim()
        ));

        return Optional.of(mapper.map(note));
    }

    @Transactional
    public Optional<LaunchNoteResponse> updateNote(String username, Long noteId, String content) {
        return userRepository.findByUsername(username)
                .flatMap(user -> noteRepository.findByIdAndUser_Id(noteId, user.getId()))
                .map(note -> {
                    note.updateContent(content.trim());
                    return mapper.map(note);
                });
    }

    @Transactional
    public boolean deleteNote(String username, Long noteId) {
        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return false;
        }

        Optional<LaunchNote> note = noteRepository.findByIdAndUser_Id(noteId, user.get().getId());

        if (note.isEmpty()) {
            return false;
        }

        noteRepository.delete(note.get());
        return true;
    }
}
