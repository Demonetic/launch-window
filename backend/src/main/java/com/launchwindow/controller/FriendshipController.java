package com.launchwindow.controller;

import com.launchwindow.config.OpenApiConfiguration;
import com.launchwindow.dto.friendship.CreateFriendRequest;
import com.launchwindow.dto.friendship.FriendshipResponse;
import com.launchwindow.service.user.FriendshipService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class FriendshipController {
    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendshipResponse sendRequest(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateFriendRequest request) {
        return service.sendRequest(jwt.getSubject(), request);
    }

    @GetMapping
    public List<FriendshipResponse> getFriends(@AuthenticationPrincipal Jwt jwt) {
        return service.getFriends(jwt.getSubject());
    }

    @GetMapping("/requests/received")
    public List<FriendshipResponse> getReceivedRequests(@AuthenticationPrincipal Jwt jwt) {
        return service.getReceivedRequests(jwt.getSubject());
    }

    @GetMapping("/requests/sent")
    public List<FriendshipResponse> getSentRequests(@AuthenticationPrincipal Jwt jwt) {
        return service.getSentRequests(jwt.getSubject());
    }

    @PatchMapping("/requests/{friendshipId}/accept")
    public FriendshipResponse accept(@AuthenticationPrincipal Jwt jwt, @PathVariable Long friendshipId) {
        return service.accept(jwt.getSubject(), friendshipId);
    }

    @PatchMapping("/requests/{friendshipId}/decline")
    public FriendshipResponse decline(@AuthenticationPrincipal Jwt jwt, @PathVariable Long friendshipId) {
        return service.decline(jwt.getSubject(), friendshipId);
    }

    @DeleteMapping("/{friendshipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long friendshipId) {
        service.remove(jwt.getSubject(), friendshipId);
    }
}