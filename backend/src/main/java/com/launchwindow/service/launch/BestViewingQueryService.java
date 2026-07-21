package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class BestViewingQueryService {
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 16;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 10;

    private final LaunchRepository repository;
    private final WeatherSummaryQueryService weatherSummaryService;
    private final Clock clock;

    public BestViewingQueryService(LaunchRepository repository, WeatherSummaryQueryService weatherSummaryService, Clock clock) {
        this.repository = repository;
        this.weatherSummaryService = weatherSummaryService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LaunchSummaryResponse> getBestViewingLaunches(int days, int limit) {
        validate(days, limit);

        Instant now = clock.instant();
        Instant endTime = now.plus(days, ChronoUnit.DAYS);

        List<Launch> launches = repository.findBestViewingLaunches(now, endTime, PageRequest.of(0, limit));

        List<Long> launchIds = launches.stream()
                .map(Launch::getId)
                .toList();

        Map<Long, WeatherSummaryResponse> weatherByLaunchId = weatherSummaryService.getByLaunchIds(launchIds);

        return launches.stream()
                .map(launch -> toSummary(launch, weatherByLaunchId.get(launch.getId())))
                .toList();
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

    private void validate(int days, int limit) {
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new InvalidPaginationException("days must be between 1 and 16");
        }

        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new InvalidPaginationException("limit must be between 1 and 10");
        }
    }
}