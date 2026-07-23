package com.launchwindow.service.calendar;

import com.launchwindow.dto.*;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class CalendarService {
    private static final int MAX_SAVED_LOOKUP_IDS = 50;

    private final AppUserRepository userRepository;
    private final LaunchRepository launchRepository;
    private final CalendarEntryRepository calendarRepository;
    private final CalendarEntryMapper mapper;
    private final WeatherSummaryQueryService weatherSummaryService;
    private final CalendarParticipantQueryService participantService;
    private final Clock clock;

    public CalendarService(AppUserRepository userRepository, LaunchRepository launchRepository, CalendarEntryRepository calendarRepository,
                           CalendarEntryMapper mapper, WeatherSummaryQueryService weatherSummaryService,
                           CalendarParticipantQueryService participantService, Clock clock) {
        this.userRepository = userRepository;
        this.launchRepository = launchRepository;
        this.calendarRepository = calendarRepository;
        this.mapper = mapper;
        this.weatherSummaryService = weatherSummaryService;
        this.participantService = participantService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CalendarPageResponse getInitialPage(String username, int limit) {
        validateLimit(limit);

        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return emptyPage();
        }

        int previousTarget = Math.min(5, limit);
        int nextTarget = limit - previousTarget;
        int querySize = limit + 1;

        List<CalendarEntry> previousCandidates = calendarRepository.findPreviousInitial(user.get().getId(), clock.instant(),
                PageRequest.of(0, querySize));
        List<CalendarEntry> nextCandidates = calendarRepository.findNextInitial(user.get().getId(), clock.instant(),
                        PageRequest.of(0, querySize));

        int previousCount = Math.min(previousTarget, previousCandidates.size());
        int nextCount = Math.min(nextTarget, nextCandidates.size());
        int missing = limit - previousCount - nextCount;
        int additionalNext = Math.min(missing, nextCandidates.size() - nextCount);

        nextCount += additionalNext;
        missing -= additionalNext;

        int additionalPrevious = Math.min(missing, previousCandidates.size() - previousCount);

        previousCount += additionalPrevious;

        List<CalendarEntry> previous = previousCandidates.stream()
                        .limit(previousCount)
                        .toList()
                        .reversed();
        List<CalendarEntry> next = nextCandidates.stream()
                        .limit(nextCount)
                        .toList();
        List<CalendarEntry> entries = Stream.concat(previous.stream(), next.stream()).toList();

        return createPage(user.get(), entries, previousCandidates.size() > previousCount, nextCandidates.size() > nextCount);
    }

    @Transactional(readOnly = true)
    public CalendarPageResponse getNextPage(String username, Instant afterTime, Long afterId, int limit) {
        validateCursor(afterTime, afterId, limit);

        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return emptyPage();
        }

        List<CalendarEntry> fetchedEntries =
                calendarRepository.findNextPage(user.get().getId(), afterTime, afterId, PageRequest.of(0, limit + 1));

        boolean hasNext = fetchedEntries.size() > limit;

        List<CalendarEntry> entries = fetchedEntries.stream()
                        .limit(limit)
                        .toList();

        return createPage(user.get(), entries, false, hasNext);
    }

    @Transactional(readOnly = true)
    public CalendarPageResponse getPreviousPage(String username, Instant beforeTime, Long beforeId, int limit) {
        validateCursor(beforeTime, beforeId, limit);

        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return emptyPage();
        }

        List<CalendarEntry> fetchedEntries =
                calendarRepository.findPreviousPage(user.get().getId(), beforeTime, beforeId, PageRequest.of(0, limit + 1));

        boolean hasPrevious = fetchedEntries.size() > limit;

        List<CalendarEntry> entries = fetchedEntries.stream()
                        .limit(limit)
                        .toList()
                        .reversed();

        return createPage(user.get(), entries, hasPrevious, false);
    }

    @Transactional(readOnly = true)
    public SavedLaunchIdsResponse getSavedLaunchIds(String username, List<Long> launchIds) {
        validateLaunchIds(launchIds);

        List<Long> distinctLaunchIds = launchIds.stream()
                        .distinct()
                        .toList();

        Optional<AppUser> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return new SavedLaunchIdsResponse(List.of());
        }

        Set<Long> savedIds = Set.copyOf(calendarRepository.findSavedLaunchIds(user.get().getId(), distinctLaunchIds));

        List<Long> orderedSavedIds = distinctLaunchIds.stream()
                        .filter(savedIds::contains)
                        .toList();

        return new SavedLaunchIdsResponse(orderedSavedIds);
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

        WeatherSummaryResponse weather = weatherSummaryService.getByLaunchIds(List.of(launchId)).get(launchId);

        List<CalendarParticipantResponse> participants = participantService.getByLaunchIds(user.get(), List.of(launchId))
                        .getOrDefault(launchId, List.of());

        return Optional.of(mapper.map(entry, weather, participants));
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

    private CalendarPageResponse createPage(AppUser user, List<CalendarEntry> entries, boolean hasPrevious, boolean hasNext) {
        if (entries.isEmpty()) {
            return emptyPage();
        }

        List<Long> launchIds = entries.stream()
                .map(CalendarEntry::getLaunch)
                .map(Launch::getId)
                .toList();

        Map<Long, WeatherSummaryResponse> weatherByLaunchId = weatherSummaryService.getByLaunchIds(launchIds);
        Map<Long, List<CalendarParticipantResponse>> participantsByLaunchId = participantService.getByLaunchIds(user, launchIds);

        List<CalendarEntryResponse> items = entries.stream()
                        .map(entry -> {Long launchId = entry.getLaunch().getId();

                            return mapper.map(entry, weatherByLaunchId.get(launchId),
                                    participantsByLaunchId.getOrDefault(launchId, List.of()));})
                        .toList();

        CalendarCursor previousCursor = cursorFrom(entries.getFirst());
        CalendarCursor nextCursor = cursorFrom(entries.getLast());

        return new CalendarPageResponse(items, previousCursor, nextCursor, hasPrevious, hasNext);
    }

    private CalendarCursor cursorFrom(CalendarEntry entry) {
        Launch launch = entry.getLaunch();

        return new CalendarCursor(launch.getLaunchTime(), launch.getId());
    }

    private CalendarPageResponse emptyPage() {
        return new CalendarPageResponse(List.of(), null, null, false, false);
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new InvalidPaginationException("Limit must be between 1 and 100");
        }
    }

    private void validateLaunchIds(List<Long> launchIds) {
        if (launchIds == null || launchIds.isEmpty() || launchIds.size() > MAX_SAVED_LOOKUP_IDS
                || launchIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw new InvalidPaginationException("Between 1 and 50 positive launch ids must be provided");
        }
    }

    private void validateCursor(Instant time, Long id, int limit) {
        validateLimit(limit);

        if (time == null || id == null || id < 1) {
            throw new InvalidPaginationException("Cursor time and id must both be provided");
        }
    }
}