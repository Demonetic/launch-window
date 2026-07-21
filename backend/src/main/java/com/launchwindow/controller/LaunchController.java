package com.launchwindow.controller;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchPageResponse;
import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.service.launch.LaunchQueryService;
import com.launchwindow.service.weather.WeatherQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@RestController
@RequestMapping("/api/launches")
public class LaunchController {
    private final LaunchQueryService launchService;
    private final WeatherQueryService weatherService;

    public LaunchController(LaunchQueryService launchService, WeatherQueryService weatherService) {
        this.launchService = launchService;
        this.weatherService = weatherService;
    }

    @GetMapping
    public LaunchPageResponse getUpcomingLaunches(@RequestParam(defaultValue = "20") int limit,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant afterTime,
                                                  @RequestParam(required = false) Long afterId) {
        return launchService.getUpcomingLaunches(
                afterTime,
                afterId,
                limit
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaunchDetailResponse> getLaunch(@PathVariable Long id) {
        return launchService.getLaunch(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/weather")
    public ResponseEntity<WeatherResponse> getWeather(@PathVariable Long id) {
        return weatherService.getLatestWeather(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
