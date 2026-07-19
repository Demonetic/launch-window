package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {
    List<WeatherSnapshot> findAllByLaunch_IdOrderByForecastTimeAsc(Long launchId);
    Optional<WeatherSnapshot> findByLaunch_IdAndForecastTime(Long launchId, Instant forecastTime);
    Optional<WeatherSnapshot> findFirstByLaunch_IdOrderByFetchedAtDesc(Long launchId);

    Long launch(Launch launch);
}
