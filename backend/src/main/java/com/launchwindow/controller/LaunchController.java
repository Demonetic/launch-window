package com.launchwindow.controller;

import com.launchwindow.dto.LaunchDetailResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.service.LaunchQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}")
    public ResponseEntity<LaunchDetailResponse> getLaunch(@PathVariable Long id) {
        return service.getLaunch(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
