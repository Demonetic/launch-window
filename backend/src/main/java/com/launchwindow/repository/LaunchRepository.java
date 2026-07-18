package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaunchRepository extends JpaRepository<Launch, Long> {
    Optional<Launch> findByExternalId(String externalId);
}
