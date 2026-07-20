package com.launchwindow.controller;

import com.launchwindow.dto.LaunchNoteRequest;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.service.note.LaunchNoteCommandService;
import com.launchwindow.service.note.LaunchNoteQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LaunchNoteController {
    private final LaunchNoteQueryService queryService;
    private final LaunchNoteCommandService commandService;

    public LaunchNoteController(LaunchNoteQueryService queryService, LaunchNoteCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping("/launches/{launchId}/notes")
    public List<LaunchNoteResponse> getNotes(@AuthenticationPrincipal Jwt jwt, @PathVariable Long launchId) {
        return queryService.getNotes(jwt.getSubject(), launchId);
    }

    @PostMapping("/launches/{launchId}/notes")
    public ResponseEntity<LaunchNoteResponse> createNote(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable Long launchId,
                                                         @Valid @RequestBody LaunchNoteRequest request) {
        return commandService.createNote(jwt.getSubject(), launchId, request.content())
                .map(note -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(note))
                .orElseGet(() -> ResponseEntity
                        .notFound()
                        .build());
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<LaunchNoteResponse> updateNote(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable Long noteId,
                                                         @Valid @RequestBody LaunchNoteRequest request) {
        return commandService.updateNote(jwt.getSubject(), noteId, request.content())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .notFound()
                        .build());
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@AuthenticationPrincipal Jwt jwt, @PathVariable Long noteId) {
        boolean deleted = commandService.deleteNote(jwt.getSubject(), noteId);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}