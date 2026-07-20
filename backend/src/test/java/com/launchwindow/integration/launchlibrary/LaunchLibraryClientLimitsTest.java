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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LaunchLibraryClient.class)
@EnableConfigurationProperties(LaunchLibraryProperties.class)
@TestPropertySource(properties = {
        "launch-library.base-url=https://launch-library.test",
        "launch-library.page-size=1",
        "launch-library.max-pages=2",
        "launch-library.max-launches=2"})
class LaunchLibraryClientLimitsTest {
    private static final String FIRST_PAGE_URL = "https://launch-library.test/launches/upcoming/" + "?limit=1&mode=normal";

    @Autowired
    private LaunchLibraryClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void fetchUpcomingLaunches_stopsAtMaxPages() {
        String secondPageUrl = "https://launch-library.test/launches/upcoming/?offset=1";

        String thirdPageUrl = "https://launch-library.test/launches/upcoming/?offset=2";

        server.expect(requestTo(FIRST_PAGE_URL))
                .andRespond(withSuccess(responseBody("launch-1", secondPageUrl), MediaType.APPLICATION_JSON));

        server.expect(requestTo(secondPageUrl))
                .andRespond(withSuccess(responseBody("launch-2", thirdPageUrl), MediaType.APPLICATION_JSON));

        List<LaunchLibraryLaunchDto> launches = client.fetchUpcomingLaunches();

        assertEquals(2, launches.size());
        assertEquals("launch-1", launches.get(0).id());
        assertEquals("launch-2", launches.get(1).id());

        server.verify();
    }

    @Test
    void fetchUpcomingLaunches_stopsAtMaxLaunches() {
        server.expect(requestTo(FIRST_PAGE_URL)).andRespond(withSuccess(responseWithThreeLaunches(), MediaType.APPLICATION_JSON));

        List<LaunchLibraryLaunchDto> launches = client.fetchUpcomingLaunches();

        assertEquals(2, launches.size());
        assertEquals("launch-1", launches.get(0).id());
        assertEquals("launch-2", launches.get(1).id());

        server.verify();
    }

    private String responseBody(String launchId, String nextUrl) {
        return """
                {
                  "count": 1,
                  "next": "%s",
                  "previous": null,
                  "results": [
                    {
                      "id": "%s",
                      "name": "Test launch",
                      "net": "2026-08-01T10:15:30Z"
                    }
                  ]
                }
                """.formatted(nextUrl, launchId);
    }

    private String responseWithThreeLaunches() {
        return """
                {
                  "count": 3,
                  "next": null,
                  "previous": null,
                  "results": [
                    {
                      "id": "launch-1",
                      "name": "First launch",
                      "net": "2026-08-01T10:15:30Z"
                    },
                    {
                      "id": "launch-2",
                      "name": "Second launch",
                      "net": "2026-08-02T10:15:30Z"
                    },
                    {
                      "id": "launch-3",
                      "name": "Third launch",
                      "net": "2026-08-03T10:15:30Z"
                    }
                  ]
                }
                """;
    }
}