package com.launchwindow.repository;

import com.launchwindow.model.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {
    Optional<WeatherSnapshot> findByLaunch_Id(Long launchId);
    List<WeatherSnapshot> findAllByLaunch_IdIn(Collection<Long> launchIds);
}
