package com.launchwindow.repository;

import com.launchwindow.model.LaunchNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaunchNoteRepository extends JpaRepository<LaunchNote, Long> {
    List<LaunchNote> findAllByUser_IdAndLaunch_IdOrderByCreatedAtDesc(Long userId, Long launchId);
    Optional<LaunchNote> findByIdAndUser_Id(Long noteId, Long userId);
}
