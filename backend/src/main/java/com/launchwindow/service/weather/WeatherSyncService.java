package com.launchwindow.service.weather;

import com.launchwindow.config.OpenMeteoProperties;
import com.launchwindow.integration.openmeteo.OpenMeteoClient;
import com.launchwindow.integration.openmeteo.WeatherForecastMapper;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.repository.WeatherSnapshotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherSyncService {
    private final OpenMeteoClient client;
    private final WeatherForecastMapper mapper;
    private final LaunchRepository launchRepository;
    private final WeatherSnapshotRepository weatherRepository;
    private final OpenMeteoProperties properties;
    private final Clock clock;

    public WeatherSyncService(OpenMeteoClient client, WeatherForecastMapper mapper, LaunchRepository launchRepository,
            WeatherSnapshotRepository weatherRepository, OpenMeteoProperties properties, Clock clock)
    {
        this.client = client;
        this.mapper = mapper;
        this.launchRepository = launchRepository;
        this.weatherRepository = weatherRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public WeatherSyncResult syncUpcomingWeather() {
        Instant fetchedAt = clock.instant();
        Instant latestLaunchTime = fetchedAt.plus(properties.forecastDays(), ChronoUnit.DAYS);

        List<Launch> launches = launchRepository
                .findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
                        fetchedAt, latestLaunchTime);

        int created = 0;
        int skipped = 0;

        for (Launch launch : launches) {
            Optional<WeatherDetails> details = fetchDetails(launch, fetchedAt);

            if (details.isEmpty()) {
                skipped++;
            } else if (saveForecast(launch, details.get())) {
                created++;
            }
        }

        int updated = launches.size() - created - skipped;

        return new WeatherSyncResult(launches.size(), created, updated, skipped);
    }

    private Optional<WeatherDetails> fetchDetails(Launch launch, Instant fetchedAt) {
        OpenMeteoResponse response = client.fetchForecast(launch.getLatitude(), launch.getLongitude());

        return mapper.map(response, launch.getLaunchTime(), fetchedAt);
    }

    private boolean saveForecast(Launch launch, WeatherDetails details) {
        Optional<WeatherSnapshot> existing = weatherRepository.findByLaunch_IdAndForecastTime(
                launch.getId(), details.forecastTime());

        if (existing.isPresent()) {
            existing.get().update(details);
            return false;
        }

        weatherRepository.save(new WeatherSnapshot(launch, details));
        return true;
    }
}