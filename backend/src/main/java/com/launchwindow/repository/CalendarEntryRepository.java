package com.launchwindow.repository;

import com.launchwindow.model.CalendarEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {

    @Query("""
            SELECT entry
            FROM CalendarEntry entry
            JOIN FETCH entry.launch launch
            WHERE entry.user.id = :userId
              AND launch.launchTime < :now
            ORDER BY launch.launchTime DESC, launch.id DESC
            """)
    List<CalendarEntry> findPreviousInitial(@Param("userId") Long userId, @Param("now") Instant now, Pageable pageable);

    @Query("""
            SELECT entry
            FROM CalendarEntry entry
            JOIN FETCH entry.launch launch
            WHERE entry.user.id = :userId
              AND launch.launchTime >= :now
            ORDER BY launch.launchTime ASC, launch.id ASC
            """)
    List<CalendarEntry> findNextInitial(@Param("userId") Long userId, @Param("now") Instant now, Pageable pageable);

    @Query("""
            SELECT entry
            FROM CalendarEntry entry
            JOIN FETCH entry.launch launch
            WHERE entry.user.id = :userId
              AND (
                    launch.launchTime < :beforeTime
                    OR (launch.launchTime = :beforeTime AND launch.id < :beforeId)
              )
            ORDER BY launch.launchTime DESC, launch.id DESC
            """)
    List<CalendarEntry> findPreviousPage(@Param("userId") Long userId, @Param("beforeTime") Instant beforeTime,
                                         @Param("beforeId") Long beforeId, Pageable pageable);

    @Query("""
            SELECT entry
            FROM CalendarEntry entry
            JOIN FETCH entry.launch launch
            WHERE entry.user.id = :userId
              AND (
                    launch.launchTime > :afterTime
                    OR (launch.launchTime = :afterTime AND launch.id > :afterId)
              )
            ORDER BY launch.launchTime ASC, launch.id ASC
            """)
    List<CalendarEntry> findNextPage(@Param("userId") Long userId, @Param("afterTime") Instant afterTime,
                                     @Param("afterId") Long afterId, Pageable pageable);

    @Query("""
        SELECT entry.launch.id
        FROM CalendarEntry entry
        WHERE entry.user.id = :userId
          AND entry.launch.id IN :launchIds
        """)
    List<Long> findSavedLaunchIds(@Param("userId") Long userId, @Param("launchIds") List<Long> launchIds);

    Optional<CalendarEntry> findByUser_IdAndLaunch_Id(Long userId, Long launchId);
}