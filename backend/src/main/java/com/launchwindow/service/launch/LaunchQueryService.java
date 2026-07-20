package com.launchwindow.service.launch;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.service.weather.WeatherSummaryQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LaunchQueryService {
    private final LaunchRepository repository;
    private final WeatherSummaryQueryService weatherSummaryService;
    private final Clock clock;

    public LaunchQueryService(LaunchRepository repository, WeatherSummaryQueryService weatherSummaryService, Clock clock) {
        this.repository = repository;
        this.weatherSummaryService = weatherSummaryService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LaunchSummaryResponse> getUpcomingLaunches() {
        List<Launch> launches = repository.findAllByLaunchTimeAfterOrderByLaunchTimeAsc(clock.instant());

        List<Long> launchIds = launches.stream()
                .map(Launch::getId)
                .toList();

        Map<Long, WeatherSummaryResponse> weatherByLaunchId = weatherSummaryService.getByLaunchIds(launchIds);

        return launches.stream()
                .map(launch -> toSummary(launch, weatherByLaunchId.get(launch.getId())))
                .toList();
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
}
