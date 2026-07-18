package com.launchwindow.service;

import com.launchwindow.integration.launchlibrary.LaunchLibraryClient;
import com.launchwindow.integration.launchlibrary.LaunchMapper;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.repository.LaunchRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class LaunchSyncService {
    private final LaunchLibraryClient client;
    private final LaunchMapper mapper;
    private final LaunchRepository repository;
    private final Clock clock;

    public LaunchSyncService(
            LaunchLibraryClient client,
            LaunchMapper mapper,
            LaunchRepository repository,
            Clock clock
    ) {
        this.client = client;
        this.mapper = mapper;
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public LaunchSyncResult syncUpcomingLaunches() {
        LaunchLibraryResponse response = client.fetchUpcomingLaunches();

        List<LaunchLibraryLaunchDto> launches = response.results() == null
                ? List.of()
                : response.results();

        Instant syncedAt = clock.instant();
        int created = 0;

        for (LaunchLibraryLaunchDto source : launches) {
            LaunchDetails details = mapper.map(source, syncedAt);
            Optional<Launch> existing = repository.findByExternalId(details.externalId());

            if (existing.isPresent()) {
                existing.get().updateFrom(details);
            } else {
                repository.save(new Launch(details));
                created++;
            }
        }

        return new LaunchSyncResult(
                launches.size(),
                created,
                launches.size() - created
        );
    }
}
