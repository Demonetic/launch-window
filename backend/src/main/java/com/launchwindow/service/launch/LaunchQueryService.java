package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.launchwindow.dto.LaunchCursor;
import com.launchwindow.dto.LaunchPageResponse;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LaunchQueryService {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;
    private final LaunchRepository repository;
    private final WeatherSummaryQueryService weatherSummaryService;
    private final Clock clock;

    public LaunchQueryService(LaunchRepository repository, WeatherSummaryQueryService weatherSummaryService, Clock clock) {
        this.repository = repository;
        this.weatherSummaryService = weatherSummaryService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public LaunchPageResponse getUpcomingLaunches(Instant afterTime, Long afterId, int limit) {
        validatePagination(afterTime, afterId, limit);

        List<Launch> fetchedLaunches = repository.findUpcomingPage(clock.instant(), afterTime,
                afterId, PageRequest.of(0, limit + 1));

        boolean hasNext = fetchedLaunches.size() > limit;

        List<Launch> pageLaunches = fetchedLaunches.stream()
                .limit(limit)
                .toList();

        List<Long> launchIds = pageLaunches.stream()
                .map(Launch::getId)
                .toList();

        Map<Long, WeatherSummaryResponse> weatherByLaunchId = weatherSummaryService.getByLaunchIds(launchIds);

        List<LaunchSummaryResponse> items = pageLaunches.stream()
                .map(launch -> toSummary(launch, weatherByLaunchId.get(launch.getId())))
                .toList();

        LaunchCursor nextCursor = hasNext
                ? toCursor(pageLaunches.getLast())
                : null;

        return new LaunchPageResponse(items, nextCursor, hasNext);
    }

    @Transactional
    public Optional<LaunchDetailResponse> getLaunch(Long id) {
        return repository.findById(id)
                .map(this::toDetail);
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
                launch.getLatitude(),
                launch.getLongitude(),
                launch.getLastSyncedAt()
        );
    }

    private void validatePagination(Instant afterTime, Long afterId, int limit) {
        boolean onlyOneCursorPartProvided = (afterTime == null) != (afterId == null);

        if (onlyOneCursorPartProvided) {
            throw new InvalidPaginationException("afterTime and afterId must be provided together");
        }

        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new InvalidPaginationException("limit must be between 1 and 50");
        }
    }

    private LaunchCursor toCursor(Launch launch) {
        return new LaunchCursor(launch.getLaunchTime(), launch.getId());
    }
}
