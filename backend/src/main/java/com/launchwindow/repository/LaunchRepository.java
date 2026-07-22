package com.launchwindow.repository;

import com.launchwindow.dto.CountryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LaunchRepository extends JpaRepository<Launch, Long> {
    Optional<Launch> findByExternalId(String externalId);
    List<Launch> findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
            Instant earliestLaunchTime, Instant latestLaunchTime);

    @Query("""
        SELECT launch
        FROM WeatherSnapshot weather
        JOIN weather.launch launch
        WHERE launch.launchTime > :now
          AND launch.launchTime <= :endTime
        ORDER BY weather.viewingScore DESC,
                 launch.launchTime ASC,
                 launch.id ASC
        """)
    List<Launch> findBestViewingLaunches(@Param("now") Instant now, @Param("endTime") Instant endTime, Pageable pageable);

    @Query("""
    SELECT DISTINCT new com.launchwindow.dto.CountryResponse(
        launch.countryCode,
        launch.countryName
    )
    FROM Launch launch
    WHERE launch.launchTime > :now
      AND launch.countryCode IS NOT NULL
      AND launch.countryName IS NOT NULL
    ORDER BY launch.countryName ASC
    """)
    List<CountryResponse> findUpcomingCountries(@Param("now") Instant now);

    @Query("""
        SELECT launch
        FROM Launch launch
        LEFT JOIN WeatherSnapshot weather
            ON weather.launch = launch
        WHERE launch.launchTime > :now
          AND (
                :endTime IS NULL
                OR launch.launchTime <= :endTime
              )
          AND (
                :filterStatuses = false
                OR launch.status IN :statuses
              )
          AND (
                :filterCountries = false
                OR launch.countryCode IN :countryCodes
              )
          AND (
                :queryPattern IS NULL
                OR LOWER(COALESCE(launch.name, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.organizationName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.rocketName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.missionType, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.locationName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.padName, ''))
                    LIKE :queryPattern
              )
          AND (
                :forecastAvailable IS NULL
                OR (
                    :forecastAvailable = true
                    AND weather.id IS NOT NULL
                )
                OR (
                    :forecastAvailable = false
                    AND weather.id IS NULL
                )
              )
          AND (
                :minimumViewingScore IS NULL
                OR weather.viewingScore >= :minimumViewingScore
              )
          AND (
                :afterTime IS NULL
                OR launch.launchTime > :afterTime
                OR (
                    launch.launchTime = :afterTime
                    AND launch.id > :afterId
                )
              )
        ORDER BY launch.launchTime ASC, launch.id ASC
        """)
    List<Launch> findBrowseSoonestPage(@Param("now") Instant now, @Param("endTime") Instant endTime,
                                       @Param("filterStatuses") boolean filterStatuses, @Param("statuses") Set<LaunchStatus> statuses,
                                       @Param("filterCountries") boolean filterCountries, @Param("countryCodes") Set<String> countryCodes,
                                       @Param("queryPattern") String queryPattern, @Param("forecastAvailable") Boolean forecastAvailable,
                                       @Param("minimumViewingScore") Short minimumViewingScore, @Param("afterTime") Instant afterTime,
                                       @Param("afterId") Long afterId, Pageable pageable);

    @Query("""
        SELECT launch
        FROM Launch launch
        LEFT JOIN WeatherSnapshot weather
            ON weather.launch = launch
        WHERE launch.launchTime > :now
          AND (
                :endTime IS NULL
                OR launch.launchTime <= :endTime
              )
          AND (
                :filterStatuses = false
                OR launch.status IN :statuses
              )
          AND (
                :filterCountries = false
                OR launch.countryCode IN :countryCodes
              )
          AND (
                :queryPattern IS NULL
                OR LOWER(COALESCE(launch.name, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.organizationName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.rocketName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.missionType, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.locationName, ''))
                    LIKE :queryPattern
                OR LOWER(COALESCE(launch.padName, ''))
                    LIKE :queryPattern
              )
          AND (
                :forecastAvailable IS NULL
                OR (
                    :forecastAvailable = true
                    AND weather.id IS NOT NULL
                )
                OR (
                    :forecastAvailable = false
                    AND weather.id IS NULL
                )
              )
          AND (
                :minimumViewingScore IS NULL
                OR weather.viewingScore >= :minimumViewingScore
              )
          AND (
                :afterViewingScore IS NULL
                OR COALESCE(weather.viewingScore, -1)
                    < :afterViewingScore
                OR (
                    COALESCE(weather.viewingScore, -1)
                        = :afterViewingScore
                    AND launch.launchTime > :afterTime
                )
                OR (
                    COALESCE(weather.viewingScore, -1)
                        = :afterViewingScore
                    AND launch.launchTime = :afterTime
                    AND launch.id > :afterId
                )
              )
        ORDER BY COALESCE(weather.viewingScore, -1) DESC,
                 launch.launchTime ASC,
                 launch.id ASC
        """)
    List<Launch> findBrowseBestViewingPage(@Param("now") Instant now, @Param("endTime") Instant endTime,
                                           @Param("filterStatuses") boolean filterStatuses, @Param("statuses") Set<LaunchStatus> statuses,
                                           @Param("filterCountries") boolean filterCountries, @Param("countryCodes") Set<String> countryCodes,
                                           @Param("queryPattern") String queryPattern, @Param("forecastAvailable") Boolean forecastAvailable,
                                           @Param("minimumViewingScore") Short minimumViewingScore, @Param("afterViewingScore") Short afterViewingScore,
                                           @Param("afterTime") Instant afterTime, @Param("afterId") Long afterId, Pageable pageable);
}
