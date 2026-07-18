package com.launchwindow.controller;

import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.service.LaunchQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/launches")
public class LaunchController {
    private final LaunchQueryService service;

    public LaunchController(LaunchQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LaunchSummaryResponse> getUpcomingLaunches() {
        return service.getUpcomingLaunches();
    }

}
