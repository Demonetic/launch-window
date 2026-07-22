package com.launchwindow.service.launch;

import com.launchwindow.dto.*;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class LaunchQueryService {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;
    private static final int MAX_DAYS = 365;
    private static final int MAX_QUERY_LENGTH = 100;

    private final LaunchRepository repository;
    private final WeatherSummaryQueryService weatherSummaryService;
    private final Clock clock;

    public LaunchQueryService(LaunchRepository repository, WeatherSummaryQueryService weatherSummaryService, Clock clock) {
        this.repository = repository;
        this.weatherSummaryService = weatherSummaryService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LaunchPageResponse browseUpcomingLaunches(LaunchBrowseFilter filter, Instant afterTime, Long afterId,
                                                     Short afterViewingScore, int limit) {
        validateBrowse(filter, afterTime, afterId, afterViewingScore, limit);

        Instant now = clock.instant();
        Instant endTime = filter.days() == null
                ? null
                : now.plus(filter.days(), ChronoUnit.DAYS);

        boolean filterStatuses = !filter.statuses().isEmpty();
        boolean filterCountries = !filter.countryCodes().isEmpty();

        Set<LaunchStatus> queryStatuses = filterStatuses
                        ? filter.statuses()
                        : EnumSet.allOf(LaunchStatus.class);
        Set<String> queryCountryCodes = filterCountries
                ? filter.countryCodes()
                : Set.of("___");

        String queryPattern = filter.query() == null
                ? null
                : "%"
                + filter.query()
                .toLowerCase(Locale.ROOT)
                + "%";

        PageRequest pageRequest = PageRequest.of(0, limit + 1);

        List<Launch> fetchedLaunches =
                filter.sort() == LaunchSort.BEST_VIEWING
                        ? repository
                        .findBrowseBestViewingPage(now, endTime, filterStatuses, queryStatuses, filterCountries, queryCountryCodes,
                                queryPattern, filter.forecastAvailable(), filter.minimumViewingScore(), afterViewingScore,
                                afterTime, afterId, pageRequest)
                        : repository
                        .findBrowseSoonestPage( now, endTime, filterStatuses, queryStatuses, filterCountries, queryCountryCodes,
                                queryPattern, filter.forecastAvailable(), filter.minimumViewingScore(), afterTime, afterId, pageRequest);

        return createPage(fetchedLaunches, limit, filter.sort());
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getUpcomingCountries() {
        return repository.findUpcomingCountries(clock.instant());
    }

    @Transactional
    public Optional<LaunchDetailResponse> getLaunch(Long id) {
        return repository.findById(id)
                .map(this::toDetail);
    }

    private LaunchPageResponse createPage(List<Launch> fetchedLaunches, int limit, LaunchSort sort) {
        boolean hasNext = fetchedLaunches.size() > limit;

        List<Launch> pageLaunches = fetchedLaunches.stream()
                        .limit(limit)
                        .toList();

        List<Long> launchIds = pageLaunches.stream()
                        .map(Launch::getId)
                        .toList();

        Map<Long, WeatherSummaryResponse> weatherByLaunchId = weatherSummaryService.getByLaunchIds(launchIds);

        List<LaunchSummaryResponse> items =
                pageLaunches.stream()
                        .map(launch -> toSummary(launch, weatherByLaunchId.get(launch.getId())))
                        .toList();

        LaunchCursor nextCursor = hasNext
                ? toCursor(pageLaunches.getLast(), sort, weatherByLaunchId.get(pageLaunches.getLast().getId()))
                : null;

        return new LaunchPageResponse(items, nextCursor, hasNext);
    }

    private LaunchSummaryResponse toSummary(Launch launch, WeatherSummaryResponse weather) {
        return new LaunchSummaryResponse(
                launch.getId(),
                launch.getName(),
                launch.getStatus(),
                launch.getLaunchTime(),
                launch.getImageUrl(),
                launch.getRocketName(),
                launch.getOrganizationName(),
                launch.getPadName(),
                launch.getLocationName(),
                launch.getCountryCode(),
                launch.getCountryName(),
                weather
        );
    }

    private LaunchDetailResponse toDetail(Launch launch) {
        return new LaunchDetailResponse(
                launch.getId(),
                launch.getName(),
                launch.getDescription(),
                launch.getStatus(),
                launch.getLaunchTime(),
                launch.getImageUrl(),
                launch.getWebcastUrl(),
                launch.getRocketName(),
                launch.getMissionType(),
                launch.getOrganizationName(),
                launch.getPadName(),
                launch.getLocationName(),
                launch.getCountryCode(),
                launch.getCountryName(),
                launch.getLatitude(),
                launch.getLongitude(),
                launch.getLastSyncedAt()
        );
    }

    private LaunchCursor toCursor(Launch launch, LaunchSort sort, WeatherSummaryResponse weather) {
        Short viewingScore =
                sort == LaunchSort.BEST_VIEWING
                        ? weather == null
                        ? (short) -1
                        : weather.viewingScore()
                        : null;

        return new LaunchCursor(launch.getLaunchTime(), launch.getId(), viewingScore);
    }

    private void validateBrowse(LaunchBrowseFilter filter, Instant afterTime, Long afterId, Short afterViewingScore, int limit) {
        validateLimit(limit);
        boolean invalidCountryCode = filter.countryCodes()
                .stream()
                .anyMatch(code -> !code.matches("[A-Z]{3}"));

        if (invalidCountryCode) {
            throw new InvalidPaginationException("countryCodes must contain ISO alpha-3 codes");
        }

        if (filter.days() != null && (filter.days() < 1 || filter.days() > MAX_DAYS)) {
            throw new InvalidPaginationException("days must be between 1 and 365");
        }

        if (filter.query() != null && filter.query().length() > MAX_QUERY_LENGTH) {
            throw new InvalidPaginationException("query must not exceed 100 characters");
        }

        if (filter.minimumViewingScore() != null && (filter.minimumViewingScore() < 0 || filter.minimumViewingScore() > 100)) {
            throw new InvalidPaginationException("minimumViewingScore must be between 0 and 100");
        }

        if (Boolean.FALSE.equals(filter.forecastAvailable()) && filter.minimumViewingScore() != null) {
            throw new InvalidPaginationException("minimumViewingScore cannot be used when forecastAvailable is false");
        }

        if (filter.sort() == LaunchSort.BEST_VIEWING) {
            validateBestViewingCursor(afterTime, afterId, afterViewingScore);
        } else {
            validateSoonestCursor(afterTime, afterId, afterViewingScore);
        }
    }

    private void validateSoonestCursor(Instant afterTime, Long afterId, Short afterViewingScore) {
        if (afterViewingScore != null) {
            throw new InvalidPaginationException("afterViewingScore is only valid for BEST_VIEWING");
        }

        boolean onlyOneCursorPartProvided = (afterTime == null) != (afterId == null);

        if (onlyOneCursorPartProvided || afterId != null && afterId < 1) {
            throw new InvalidPaginationException("afterTime and afterId must be provided together");
        }
    }

    private void validateBestViewingCursor(Instant afterTime, Long afterId, Short afterViewingScore) {
        boolean anyCursorPartProvided = afterTime != null || afterId != null || afterViewingScore != null;

        boolean allCursorPartsProvided = afterTime != null && afterId != null && afterViewingScore != null;

        if (anyCursorPartProvided && !allCursorPartsProvided) {
            throw new InvalidPaginationException("BEST_VIEWING cursor requires score, time and id");
        }

        if (afterId != null && afterId < 1) {
            throw new InvalidPaginationException("afterId must be positive");
        }

        if (afterViewingScore != null && (afterViewingScore < -1 || afterViewingScore > 100)) {
            throw new InvalidPaginationException("afterViewingScore must be between -1 and 100");
        }
    }

    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new InvalidPaginationException("limit must be between 1 and 50");
        }
    }
}