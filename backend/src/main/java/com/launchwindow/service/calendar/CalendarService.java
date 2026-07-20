package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {
    private final AppUserRepository userRepository;
    private final LaunchRepository launchRepository;
    private final CalendarEntryRepository calendarRepository;
    private final CalendarEntryMapper mapper;

    public CalendarService(AppUserRepository userRepository, LaunchRepository launchRepository,
            CalendarEntryRepository calendarRepository, CalendarEntryMapper mapper) {
        this.userRepository = userRepository;
        this.launchRepository = launchRepository;
        this.calendarRepository = calendarRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CalendarEntryResponse> getCalendar(String username) {
        return userRepository.findByUsername(username)
                .map(user -> calendarRepository.findAllByUser_IdOrderBySavedAtDesc(user.getId())
                        .stream()
                        .map(mapper::map)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional
    public Optional<CalendarEntryResponse> saveLaunch(String username, Long launchId) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        Optional<Launch> launch = launchRepository.findById(launchId);

        if (user.isEmpty() || launch.isEmpty()) {
            return Optional.empty();
        }

        CalendarEntry entry = calendarRepository.findByUser_IdAndLaunch_Id(user.get().getId(), launchId)
                .orElseGet(() -> calendarRepository.save(new CalendarEntry(user.get(), launch.get())));

        return Optional.of(mapper.map(entry));
    }

    @Transactional
    public boolean removeLaunch(String username, Long launchId) {
        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return false;
        }

        Optional<CalendarEntry> entry = calendarRepository.findByUser_IdAndLaunch_Id(user.get().getId(), launchId);

        if (entry.isEmpty()) {
            return false;
        }

        calendarRepository.delete(entry.get());
        return true;
    }
}