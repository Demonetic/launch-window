package com.launchwindow.repository;

import com.launchwindow.model.CalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {
    List<CalendarEntry> findAllByUser_IdOrderBySavedAtDesc(Long userId);
    Optional<CalendarEntry> findByUser_IdAndLaunch_Id(Long userId, Long launchId);
}
