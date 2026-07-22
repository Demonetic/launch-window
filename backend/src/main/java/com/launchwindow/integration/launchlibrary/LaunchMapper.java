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
    private final CountryNameResolver countryNameResolver;
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    private final LaunchStatusMapper statusMapper;

    public LaunchMapper(LaunchStatusMapper statusMapper, CountryNameResolver countryNameResolver) {
        this.statusMapper = statusMapper;
        this.countryNameResolver = countryNameResolver;
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
                countryCode(source),
                countryName(source),
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
        BigDecimal latitude = source.pad() == null
                ? null
                : source.pad().latitude();

        return isWithinRange(latitude, MIN_LATITUDE, MAX_LATITUDE)
                ? latitude
                : null;
    }

    private String countryCode(LaunchLibraryLaunchDto source) {
        if (source.pad() == null) {
            return null;
        }

        if (source.pad().country() != null) {
            String padCountryCode = source.pad().country().alpha3Code();

            if (padCountryCode != null && !padCountryCode.isBlank()) {
                return countryNameResolver.normalize(padCountryCode);
            }
        }

        if (source.pad().location() == null) {
            return null;
        }

        return countryNameResolver.normalize(source.pad().location().countryCode());
    }

    private String countryName(LaunchLibraryLaunchDto source) {
        return countryNameResolver.resolve(countryCode(source));
    }

    private BigDecimal longitude(LaunchLibraryLaunchDto source) {
        BigDecimal longitude = source.pad() == null
                ? null
                : source.pad().longitude();

        return isWithinRange(longitude, MIN_LONGITUDE, MAX_LONGITUDE)
                ? longitude
                : null;
    }

    private boolean isWithinRange(BigDecimal value, BigDecimal minimum, BigDecimal maximum) {
        return value != null && value.compareTo(minimum) >= 0 && value.compareTo(maximum) <= 0;
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
