package com.launchwindow.integration.openmeteo;

import com.launchwindow.config.OpenMeteoProperties;
import com.launchwindow.integration.openmeteo.dto.OpenMeteoResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class OpenMeteoClient {
    private static final String HOURLY_VARIABLES = String.join(
            ",",
            "temperature_2m",
            "cloud_cover",
            "precipitation_probability",
            "wind_speed_10m",
            "visibility"
    );

    private final RestClient restClient;
    private final int forecastDays;

    public OpenMeteoClient(
            RestClient.Builder restClientBuilder, OpenMeteoProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
        this.forecastDays = properties.forecastDays();
    }

    public OpenMeteoResponse fetchForecast(BigDecimal latitude, BigDecimal longitude) {
        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", HOURLY_VARIABLES)
                        .queryParam("forecast_days", forecastDays)
                        .queryParam("timeformat", "unixtime")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OpenMeteoResponse.class);

        return Objects.requireNonNull(response, "Open-Meteo returned an empty response");
    }
}
