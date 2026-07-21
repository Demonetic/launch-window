package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LaunchRepository extends JpaRepository<Launch, Long> {
    Optional<Launch> findByExternalId(String externalId);
    List<Launch> findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
            Instant earliestLaunchTime, Instant latestLaunchTime);
    @Query("""
        SELECT launch
        FROM Launch launch
        WHERE launch.launchTime > :now
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
    List<Launch> findUpcomingPage(
            @Param("now") Instant now,
            @Param("afterTime") Instant afterTime,
            @Param("afterId") Long afterId,
            Pageable pageable
    );

}
