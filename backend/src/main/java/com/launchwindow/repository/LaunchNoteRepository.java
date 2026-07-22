package com.launchwindow.repository;

import com.launchwindow.model.LaunchNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LaunchNoteRepository extends JpaRepository<LaunchNote, Long> {
    List<LaunchNote> findAllByUser_IdAndLaunch_IdOrderByCreatedAtDesc(Long userId, Long launchId);
    Optional<LaunchNote> findByIdAndUser_Id(Long noteId, Long userId);

    @Query("""
            SELECT note
            FROM LaunchNote note
            JOIN FETCH note.launch
            WHERE note.user.id = :userId
            ORDER BY note.updatedAt DESC, note.id DESC
            """)
    List<LaunchNote> findOverviewInitial(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT note
            FROM LaunchNote note
            JOIN FETCH note.launch
            WHERE note.user.id = :userId
              AND (
                    note.updatedAt < :beforeUpdatedAt
                    OR (
                        note.updatedAt = :beforeUpdatedAt
                        AND note.id < :beforeId
                    )
                  )
            ORDER BY note.updatedAt DESC, note.id DESC
            """)
    List<LaunchNote> findOverviewPage(@Param("userId") Long userId, @Param("beforeUpdatedAt") Instant beforeUpdatedAt,
                                      @Param("beforeId") Long beforeId, Pageable pageable);
}