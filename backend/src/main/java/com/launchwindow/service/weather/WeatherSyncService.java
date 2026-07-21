package com.launchwindow.service.weather;

import com.launchwindow.config.OpenMeteoProperties;
import com.launchwindow.exception.WeatherProviderException;
import com.launchwindow.integration.openmeteo.OpenMeteoClient;
import com.launchwindow.integration.openmeteo.WeatherForecastMapper;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.LaunchRepository;
import com.launchwindow.repository.WeatherSnapshotRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherSyncService.class);
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
        try {
            OpenMeteoResponse response = client.fetchForecast(launch.getLatitude(), launch.getLongitude());

            return mapper.map(response, launch.getLaunchTime(), fetchedAt);
        } catch (WeatherProviderException exception) {
            LOGGER.warn(
                    "Weather synchronization failed for launch {}: {}",
                    launch.getExternalId(),
                    exception.getMessage()
            );

            return Optional.empty();
        }
    }
    private boolean saveForecast(Launch launch, WeatherDetails details) {
        Optional<WeatherSnapshot> existing = weatherRepository.findByLaunch_Id(launch.getId());

        if (existing.isPresent()) {
            existing.get().update(details);
            return false;
        }

        weatherRepository.save(new WeatherSnapshot(launch, details));

        return true;
    }
}