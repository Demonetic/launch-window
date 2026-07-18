package com.launchwindow.integration.launchlibrary;

import com.launchwindow.config.LaunchLibraryProperties;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class LaunchLibraryClient {
    private static final String UPCOMING_LAUNCHES_PATH = "/launches/upcoming/";

    private final RestClient restClient;
    private final int pageSize;

    public LaunchLibraryClient(RestClient.Builder restClientBuilder, LaunchLibraryProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
        this.pageSize = properties.pageSize();
    }

    public LaunchLibraryResponse fetchUpcomingLaunches() {
        LaunchLibraryResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(UPCOMING_LAUNCHES_PATH)
                        .queryParam("limit", pageSize)
                        .queryParam("mode", "normal")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(LaunchLibraryResponse.class);

        return Objects.requireNonNull(response, "Launch Library returned an empty response");
    }
}
