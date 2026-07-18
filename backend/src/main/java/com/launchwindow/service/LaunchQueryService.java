package com.launchwindow.service;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.repository.LaunchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Service
public class LaunchQueryService {
    private final LaunchRepository repository;
    private final Clock clock;

    public LaunchQueryService(LaunchRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LaunchSummaryResponse> getUpcomingLaunches() {
        return repository.findAllByLaunchTimeAfterOrderByLaunchTimeAsc(
                clock.instant())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public Optional<LaunchDetailResponse> getLaunch(Long id) {
        return repository.findById(id)
                .map(this::toDetail);
    }

    private LaunchSummaryResponse toSummary(Launch launch) {
        return new LaunchSummaryResponse(
                launch.getId(),
                launch.getName(),
                launch.getStatus(),
                launch.getLaunchTime(),
                launch.getImageUrl(),
                launch.getRocketName(),
                launch.getOrganizationName(),
                launch.getPadName(),
                launch.getLocationName()
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
