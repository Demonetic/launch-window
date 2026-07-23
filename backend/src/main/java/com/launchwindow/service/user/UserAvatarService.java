package com.launchwindow.service.user;

import com.launchwindow.dto.UpdateAvatarRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserAvatarService {
    private final AppUserRepository repository;

    public UserAvatarService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserResponse updateAvatar(String username, UpdateAvatarRequest request) {
        AppUser user = repository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user was not found"));

        String normalizedColor = request.avatarColor().toUpperCase(Locale.ROOT);

        user.updateAvatar(request.avatarKey(), normalizedColor);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarKey(),
                user.getAvatarColor()
        );
    }
}