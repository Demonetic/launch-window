package com.launchwindow.integration.launchlibrary;

import com.launchwindow.config.LaunchLibraryProperties;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryLaunchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LaunchLibraryClient.class)
@EnableConfigurationProperties(LaunchLibraryProperties.class)
@TestPropertySource(properties = {
        "launch-library.base-url=https://launch-library.test",
        "launch-library.page-size=10",
        "launch-library.max-pages=3",
        "launch-library.max-launches=25"})
class LaunchLibraryClientTest {
    @Autowired
    private LaunchLibraryClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void fetchUpcomingLaunches_returnsMappedResponse() {
        String responseBody = """
                {
                  "count": 1,
                  "next": null,
                  "previous": null,
                  "results": [
                    {
                      "id": "launch-123",
                      "name": "Artemis Test Launch",
                      "status": {
                        "id": 1,
                        "name": "Go",
                        "abbrev": "Go"
                      },
                      "net": "2026-08-01T10:15:30Z",
                      "image": {
                        "image_url": "https://example.com/launch.jpg"
                      }
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://launch-library.test/launches/upcoming/" + "?limit=10&mode=normal"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<LaunchLibraryLaunchDto> launches = client.fetchUpcomingLaunches();

        assertEquals(1, launches.size());
        assertEquals("launch-123", launches.getFirst().id());
        assertNotNull(launches.getFirst().net());

        server.verify();
    }

    @Test
    void fetchUpcomingLaunches_combinesMultiplePages() {
        String secondPageUrl = "https://launch-library.test/launches/upcoming/" + "?limit=10&offset=10&mode=normal";

        server.expect(requestTo("https://launch-library.test/launches/upcoming/" + "?limit=10&mode=normal"))
                .andRespond(withSuccess(responseBody("launch-1", secondPageUrl), MediaType.APPLICATION_JSON));

        server.expect(requestTo(secondPageUrl))
                .andRespond(withSuccess(responseBody("launch-2", null), MediaType.APPLICATION_JSON));

        List<LaunchLibraryLaunchDto> launches = client.fetchUpcomingLaunches();

        assertEquals(2, launches.size());
        assertEquals("launch-1", launches.get(0).id());
        assertEquals("launch-2", launches.get(1).id());

        server.verify();
    }

    @Test
    void fetchUpcomingLaunches_rejectsNextUrlFromDifferentHost() {
        server.expect(requestTo("https://launch-library.test/launches/upcoming/" + "?limit=10&mode=normal"))
                .andRespond(withSuccess(responseBody("launch-1", "https://untrusted.example/launches/upcoming/"),
                        MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class, client::fetchUpcomingLaunches);

        assertEquals("Launch Library returned an untrusted pagination URL", exception.getMessage());

        server.verify();
    }

    @Test
    void fetchUpcomingLaunches_rejectsRepeatedPaginationUrl() {
        String repeatedUrl = "https://launch-library.test/launches/upcoming/" + "?limit=10&offset=10&mode=normal";

        server.expect(requestTo("https://launch-library.test/launches/upcoming/" + "?limit=10&mode=normal"))
                .andRespond(withSuccess(responseBody("launch-1", repeatedUrl), MediaType.APPLICATION_JSON));

        server.expect(requestTo(repeatedUrl)).andRespond(withSuccess(responseBody("launch-2", repeatedUrl),
                        MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class, client::fetchUpcomingLaunches);

        assertEquals("Launch Library returned a repeated pagination URL", exception.getMessage());

        server.verify();
    }

    @Test
    void fetchRecentLaunches_requestsSelectedTimeWindow() {
        Instant from = Instant.parse("2026-07-19T12:00:00Z");
        Instant until = Instant.parse("2026-07-21T12:00:00Z");

        String expectedUrl =
                "https://launch-library.test/launches/"
                        + "?limit=10"
                        + "&mode=normal"
                        + "&net__gte=2026-07-19T12:00:00Z"
                        + "&net__lte=2026-07-21T12:00:00Z"
                        + "&ordering=net";

        server.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(responseBody("recent-launch", null), MediaType.APPLICATION_JSON));

        List<LaunchLibraryLaunchDto> result = client.fetchRecentLaunches(from, until);

        assertEquals(1, result.size());
        assertEquals("recent-launch", result.getFirst().id());

        server.verify();
    }

    @Test
    void fetchRecentLaunches_rejectsReversedTimeWindow() {
        Instant from = Instant.parse("2026-07-21T12:00:00Z");
        Instant until = Instant.parse("2026-07-19T12:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> client.fetchRecentLaunches(from, until));
    }

    private String responseBody(String launchId, String nextUrl) {
        String nextValue = nextUrl == null
                ? "null"
                : "\"" + nextUrl + "\"";

        return """
            {
              "count": 1,
              "next": %s,
              "previous": null,
              "results": [
                {
                  "id": "%s",
                  "name": "Test launch",
                  "net": "2026-08-01T10:15:30Z"
                }
              ]
            }
            """.formatted(nextValue, launchId);
    }
}