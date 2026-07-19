package com.launchwindow.service;

import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.WeatherSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WeatherQueryService {
    private final WeatherSnapshotRepository repository;

    public WeatherQueryService(WeatherSnapshotRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<WeatherResponse> getLatestWeather(Long launchId) {
        return repository.findFirstByLaunch_IdOrderByFetchedAtDesc(launchId)
                .map(this::toResponse);
    }

    private WeatherResponse toResponse(WeatherSnapshot snapshot) {
        return new WeatherResponse(
                snapshot.getLaunch().getId(),
                snapshot.getForecastTime(),
                snapshot.getTemperatureC(),
                snapshot.getCloudCoverPercent(),
                snapshot.getPrecipitationProbabilityPercent(),
                snapshot.getWindSpeedKmh(),
                snapshot.getVisibilityMeters(),
                snapshot.getViewingScore(),
                snapshot.getFetchedAt()
        );
    }
}
