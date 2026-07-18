package com.launchwindow.integration.launchlibrary;

import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryVideoDto;
import com.launchwindow.model.LaunchDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class LaunchMapper {
    private static final String UNKNOWN_ROCKET = "Unknown rocket";

    private final LaunchStatusMapper statusMapper;

    public LaunchMapper(LaunchStatusMapper statusMapper) {
        this.statusMapper = statusMapper;
    }

    public LaunchDetails map(LaunchLibraryLaunchDto source, Instant syncedAt) {
        Objects.requireNonNull(source, "Launch data is required");
        Objects.requireNonNull(syncedAt, "Sync time is required");

        return new LaunchDetails(
                source.id(),
                source.name(),
                missionDescription(source),
                statusMapper.map(source.status()),
                source.net(),
                imageUrl(source),
                firstVideoUrl(source.videoUrls()),
                rocketName(source),
                missionType(source),
                organizationName(source),
                padName(source),
                locationName(source),
                latitude(source),
                longitude(source),
                syncedAt
        );
    }

    private String imageUrl(LaunchLibraryLaunchDto source) {
        return source.image() == null ? null : source.image().imageUrl();
    }

    private String rocketName(LaunchLibraryLaunchDto source) {
        if (source.rocket() == null
                || source.rocket().configuration() == null
                || source.rocket().configuration().fullName() == null) {
            return UNKNOWN_ROCKET;
        }

        return source.rocket().configuration().fullName();
    }

    private String missionType(LaunchLibraryLaunchDto source) {
        return source.mission() == null ? null : source.mission().type();
    }

    private String missionDescription(LaunchLibraryLaunchDto source) {
        return source.mission() == null ? null : source.mission().description();
    }

    private String organizationName(LaunchLibraryLaunchDto source) {
        return source.launchServiceProvider() == null ? null : source.launchServiceProvider().name();
    }

    private String padName(LaunchLibraryLaunchDto source) {
        return source.pad() == null ? null : source.pad().name();
    }

    private String locationName(LaunchLibraryLaunchDto source) {
        return source.pad() == null || source.pad().location() == null ? null : source.pad().location().name();
    }

    private BigDecimal latitude(LaunchLibraryLaunchDto source) {
        return source.pad() == null ? null : source.pad().latitude();
    }

    private BigDecimal longitude(LaunchLibraryLaunchDto source) {
        return source.pad() == null ? null : source.pad().longitude();
    }

    private String firstVideoUrl(List<LaunchLibraryVideoDto> videos) {
        if (videos == null) {
            return null;
        }

        return videos.stream()
                .filter(Objects::nonNull)
                .map(LaunchLibraryVideoDto::url)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);
    }
}
