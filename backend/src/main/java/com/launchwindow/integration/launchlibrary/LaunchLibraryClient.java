package com.launchwindow.integration.launchlibrary;

import com.launchwindow.config.LaunchLibraryProperties;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class LaunchLibraryClient {
    private static final String UPCOMING_LAUNCHES_PATH = "/launches/upcoming/";
    private final RestClient restClient;
    private final URI baseUri;
    private final int pageSize;
    private final int maxPages;
    private final int maxLaunches;

    public LaunchLibraryClient(RestClient.Builder restClientBuilder, LaunchLibraryProperties properties) {
        this.restClient = restClientBuilder.build();
        this.baseUri = URI.create(properties.baseUrl());
        this.pageSize = properties.pageSize();
        this.maxPages = properties.maxPages();
        this.maxLaunches = properties.maxLaunches();
    }

    public List<LaunchLibraryLaunchDto> fetchUpcomingLaunches() {
        List<LaunchLibraryLaunchDto> launches = new ArrayList<>();
        Set<URI> visitedUris = new HashSet<>();
        URI nextUri = createInitialUri();

        for (int page = 0; page < maxPages && nextUri != null && launches.size() < maxLaunches; page++) {

            validateUri(nextUri);

            if (!visitedUris.add(nextUri)) {
                throw new IllegalStateException("Launch Library returned a repeated pagination URL");
            }

            LaunchLibraryResponse response = fetchPage(nextUri);
            addResults(launches, response.results());
            nextUri = resolveNextUri(response.next());
        }

        return List.copyOf(launches);
    }

    private LaunchLibraryResponse fetchPage(URI uri) {
        LaunchLibraryResponse response = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(LaunchLibraryResponse.class);

        return Objects.requireNonNull(response, "Launch Library returned an empty response");
    }

    private void addResults(List<LaunchLibraryLaunchDto> launches, List<LaunchLibraryLaunchDto> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        int remainingCapacity = maxLaunches - launches.size();
        launches.addAll(results.subList(0, Math.min(results.size(), remainingCapacity)));
    }

    private URI createInitialUri() {
        return UriComponentsBuilder.fromUri(baseUri)
                .path(UPCOMING_LAUNCHES_PATH)
                .queryParam("limit", pageSize)
                .queryParam("mode", "normal")
                .build()
                .toUri();
    }

    private URI resolveNextUri(String next) {
        if (next == null || next.isBlank()) {
            return null;
        }

        return baseUri.resolve(next);
    }

    private void validateUri(URI uri) {
        boolean sameOrigin = baseUri.getScheme().equalsIgnoreCase(uri.getScheme())
                && baseUri.getHost().equalsIgnoreCase(uri.getHost()) && baseUri.getPort() == uri.getPort();

        if (!sameOrigin) {
            throw new IllegalStateException("Launch Library returned an untrusted pagination URL");
        }
    }
}