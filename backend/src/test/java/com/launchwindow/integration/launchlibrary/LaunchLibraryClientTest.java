package com.launchwindow.integration.launchlibrary;

import com.launchwindow.config.LaunchLibraryProperties;
import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LaunchLibraryClient.class)
@EnableConfigurationProperties(LaunchLibraryProperties.class)
@TestPropertySource(properties = {
        "launch-library.base-url=https://launch-library.test",
        "launch-library.page-size=10"
})
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

        server.expect(requestTo(
                        "https://launch-library.test/launches/upcoming/"
                                + "?limit=10&mode=normal"
                ))
                .andRespond(withSuccess(
                        responseBody,
                        MediaType.APPLICATION_JSON
                ));

        LaunchLibraryResponse response = client.fetchUpcomingLaunches();

        assertEquals(1, response.count());
        assertEquals(1, response.results().size());
        assertEquals("launch-123", response.results().getFirst().id());
        assertNotNull(response.results().getFirst().net());

        server.verify();
    }
}
