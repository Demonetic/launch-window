package com.launchwindow.service.weather;

import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.WeatherSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableMap;

@Service
public class WeatherSummaryQueryService {
    private final WeatherSnapshotRepository repository;

    public WeatherSummaryQueryService(WeatherSnapshotRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherSummaryResponse> getByLaunchIds(Collection<Long> launchIds) {
        if (launchIds.isEmpty()) {
            return Map.of();
        }

        return repository.findAllByLaunch_IdIn(launchIds)
                .stream()
                .collect(toUnmodifiableMap(snapshot -> snapshot.getLaunch().getId(), this::toResponse));
    }

    private WeatherSummaryResponse toResponse(WeatherSnapshot snapshot) {
        short viewingScore = snapshot.getViewingScore();

        return new WeatherSummaryResponse(viewingScore, ViewingCondition.fromScore(viewingScore), snapshot.getForecastTime());
    }
}