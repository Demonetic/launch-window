package com.launchwindow.controller;

import com.launchwindow.dto.*;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.service.launch.BestViewingQueryService;
import com.launchwindow.service.launch.LaunchQueryService;
import com.launchwindow.service.weather.WeatherQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/launches")
public class LaunchController {
    private final LaunchQueryService launchService;
    private final WeatherQueryService weatherService;
    private final BestViewingQueryService bestViewingService;

    public LaunchController(LaunchQueryService launchService, WeatherQueryService weatherService, BestViewingQueryService bestViewingService) {
        this.launchService = launchService;
        this.weatherService = weatherService;
        this.bestViewingService = bestViewingService;
    }

    @GetMapping
    public LaunchPageResponse getUpcomingLaunches(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "SOONEST") LaunchSort sort,
                                                  @RequestParam(required = false) Integer days, @RequestParam(required = false) Set<LaunchStatus> statuses,
                                                  @RequestParam(required = false) Set<String> countryCodes, @RequestParam(required = false) String query,
                                                  @RequestParam(required = false) Boolean forecastAvailable, @RequestParam(required = false) Short minimumViewingScore,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant afterTime,
                                                  @RequestParam(required = false) Long afterId, @RequestParam(required = false) Short afterViewingScore) {
        LaunchBrowseFilter filter = new LaunchBrowseFilter(sort, days, statuses, countryCodes, query, forecastAvailable, minimumViewingScore);

        return launchService.browseUpcomingLaunches(filter, afterTime, afterId, afterViewingScore, limit);
    }

    @GetMapping("/best-viewing")
    public List<LaunchSummaryResponse> getBestViewingLaunches(@RequestParam(defaultValue = "7") int days, @RequestParam(defaultValue = "3") int limit) {
        return bestViewingService.getBestViewingLaunches(days, limit);
    }

    @GetMapping("/countries")
    public List<CountryResponse> getUpcomingCountries() {
        return launchService.getUpcomingCountries();
    }

    @GetMapping("/{id}")
    public LaunchDetailResponse getLaunch(@PathVariable Long id) {
        return launchService.getLaunch(id).orElseThrow(
                () -> new ResourceNotFoundException("Launch with id " + id + " was not found"));
    }

    @GetMapping("/{id}/weather")
    public WeatherResponse getWeather(@PathVariable Long id) {
        return weatherService.getLatestWeather(id).orElseThrow(
                () -> new ResourceNotFoundException("Weather for launch " + id + " was not found"));
    }
}