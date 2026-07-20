package com.launchwindow.controller;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.service.calendar.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService service;

    public CalendarController(CalendarService service) {
        this.service = service;
    }

    @GetMapping
    public List<CalendarEntryResponse> getCalendar(@AuthenticationPrincipal Jwt jwt) {
        return service.getCalendar(jwt.getSubject());
    }

    @PutMapping("/{launchId}")
    public ResponseEntity<CalendarEntryResponse> saveLaunch(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId) {
        return service.saveLaunch(jwt.getSubject(), launchId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .notFound()
                        .build());
    }

    @DeleteMapping("/{launchId}")
    public ResponseEntity<Void> removeLaunch(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId) {
        boolean removed = service.removeLaunch(jwt.getSubject(), launchId);

        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}