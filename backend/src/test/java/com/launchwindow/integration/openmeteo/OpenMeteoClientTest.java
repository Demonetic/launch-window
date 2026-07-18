package com.launchwindow.integration.openmeteo;

import com.launchwindow.config.OpenMeteoProperties;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(OpenMeteoClient.class)
@EnableConfigurationProperties(OpenMeteoProperties.class)
@TestPropertySource(properties = {
        "open-meteo.base-url=https://weather.test",
        "open-meteo.forecast-days=16"
})
class OpenMeteoClientTest {
    @Autowired
    private OpenMeteoClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void fetchForecast_returnsMappedHourlyWeather() {
        String responseBody = """
                {
                  "hourly": {
                    "time": [1785578400],
                    "temperature_2m": [24.5],
                    "cloud_cover": [35],
                    "precipitation_probability": [10],
                    "wind_speed_10m": [12.4],
                    "visibility": [24000]
                  }
                }
                """;

        server.expect(requestTo(
                        "https://weather.test/forecast"
                                + "?latitude=28.627000"
                                + "&longitude=-80.621000"
                                + "&hourly=temperature_2m,cloud_cover,"
                                + "precipitation_probability,"
                                + "wind_speed_10m,visibility"
                                + "&forecast_days=16"
                                + "&timeformat=unixtime"
                ))
                .andRespond(withSuccess(
                        responseBody,
                        MediaType.APPLICATION_JSON
                ));

        OpenMeteoResponse response = client.fetchForecast(
                new BigDecimal("28.627000"),
                new BigDecimal("-80.621000")
        );

        assertEquals(1, response.hourly().time().size());
        assertEquals(new BigDecimal("24.5"), response.hourly().temperatures().getFirst());
        assertEquals(35, response.hourly().cloudCoverPercentages().getFirst());

        server.verify();
    }
}
