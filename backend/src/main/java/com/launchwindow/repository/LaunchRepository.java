package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LaunchRepository extends JpaRepository<Launch, Long> {
    Optional<Launch> findByExternalId(String externalId);
    List<Launch> findAllByLaunchTimeAfterOrderByLaunchTimeAsc(Instant earliestLaunchTime);
    List<Launch> findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
            Instant earliestLaunchTime, Instant latestLaunchTime);
}
