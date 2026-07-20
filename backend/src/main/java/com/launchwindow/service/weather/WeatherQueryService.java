package com.launchwindow.service.weather;

import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.model.ViewingCondition;
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
        return repository.findByLaunch_Id(launchId)
                .map(this::toResponse);
    }

    private WeatherResponse toResponse(WeatherSnapshot snapshot) {
        short viewingScore = snapshot.getViewingScore();

        return new WeatherResponse(
                snapshot.getLaunch().getId(),
                snapshot.getForecastTime(),
                snapshot.getTemperatureC(),
                snapshot.getCloudCoverPercent(),
                snapshot.getPrecipitationProbabilityPercent(),
                snapshot.getWindSpeedKmh(),
                snapshot.getVisibilityMeters(),
                viewingScore,
                ViewingCondition.fromScore(viewingScore),
                snapshot.getFetchedAt()
        );
    }
}
