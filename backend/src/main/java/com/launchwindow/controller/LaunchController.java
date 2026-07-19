package com.launchwindow.controller;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherResponse;
import com.launchwindow.service.LaunchQueryService;
import com.launchwindow.service.WeatherQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<LaunchSummaryResponse> getUpcomingLaunches() {
        return launchService.getUpcomingLaunches();
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
