package com.launchwindow.controller;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.CalendarPageResponse;
import com.launchwindow.exception.InvalidPaginationException;
import com.launchwindow.service.calendar.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService service;

    public CalendarController(CalendarService service) {
        this.service = service;
    }

    @GetMapping
    public CalendarPageResponse getCalendar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") int limit,
                                            @RequestParam(required = false) Instant afterTime, @RequestParam(required = false) Long afterId,
                                            @RequestParam(required = false) Instant beforeTime, @RequestParam(required = false) Long beforeId) {
        boolean hasAfterCursor = afterTime != null || afterId != null;
        boolean hasBeforeCursor = beforeTime != null || beforeId != null;

        if (hasAfterCursor && hasBeforeCursor) {
            throw new InvalidPaginationException("Only one calendar cursor direction may be requested");
        }

        if (hasAfterCursor) {
            return service.getNextPage(jwt.getSubject(), afterTime, afterId, limit);
        }

        if (hasBeforeCursor) {
            return service.getPreviousPage(jwt.getSubject(), beforeTime, beforeId, limit);
        }

        return service.getInitialPage(jwt.getSubject(), limit);
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