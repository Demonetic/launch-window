package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.LaunchNoteRequest;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.service.note.LaunchNoteCommandService;
import com.launchwindow.service.note.LaunchNoteQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.launchwindow.dto.LaunchNotePageResponse;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class LaunchNoteController {
    private final LaunchNoteQueryService queryService;
    private final LaunchNoteCommandService commandService;

    public LaunchNoteController(LaunchNoteQueryService queryService, LaunchNoteCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping("/notes")
    public LaunchNotePageResponse getNotesPage(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") int limit,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                               Instant beforeUpdatedAt, @RequestParam(required = false) Long beforeId) {
        return queryService.getNotesPage(jwt.getSubject(), beforeUpdatedAt, beforeId, limit);
    }

    @GetMapping("/launches/{launchId}/notes")
    public List<LaunchNoteResponse> getNotes(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId) {
        return queryService.getNotes(jwt.getSubject(), launchId);
    }

    @PostMapping("/launches/{launchId}/notes")
    public ResponseEntity<LaunchNoteResponse> createNote(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId,
                                                         @Valid @RequestBody LaunchNoteRequest request) {
        return commandService.createNote(jwt.getSubject(), launchId, request.content())
                .map(note -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(note))
                .orElseThrow(() -> new ResourceNotFoundException("Launch with id " + launchId + " was not found"));
    }

    @PutMapping("/notes/{noteId}")
    public LaunchNoteResponse updateNote(@AuthenticationPrincipal Jwt jwt, @PathVariable Long noteId, @Valid @RequestBody LaunchNoteRequest request) {
        return commandService.updateNote(jwt.getSubject(), noteId, request.content())
                .orElseThrow(() -> new ResourceNotFoundException("Note with id " + noteId + " was not found"));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@AuthenticationPrincipal Jwt jwt, @PathVariable Long noteId) {
        boolean deleted = commandService.deleteNote(jwt.getSubject(), noteId);

        if (!deleted) {
            throw new ResourceNotFoundException("Note with id " + noteId + " was not found");
        }

        return ResponseEntity.noContent().build();
    }
}