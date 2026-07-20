package com.launchwindow.service.launch;

import com.launchwindow.integration.launchlibrary.LaunchMapper;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.repository.LaunchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class LaunchSyncWriter {
    private final LaunchMapper mapper;
    private final LaunchRepository repository;
    private final Clock clock;

    public LaunchSyncWriter(LaunchMapper mapper, LaunchRepository repository, Clock clock) {
        this.mapper = mapper;
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public LaunchSyncResult synchronize(List<LaunchLibraryLaunchDto> launches) {
        Instant syncedAt = clock.instant();
        int created = 0;

        for (LaunchLibraryLaunchDto source : launches) {
            LaunchDetails details = mapper.map(source, syncedAt);
            Optional<Launch> existing = repository.findByExternalId(details.externalId());

            if (existing.isPresent()) {existing.get().updateFrom(details);
            } else {repository.save(new Launch(details));
                created++;
            }
        }

        return new LaunchSyncResult(launches.size(), created, launches.size() - created);
    }
}