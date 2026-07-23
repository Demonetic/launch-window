package com.launchwindow.service.user;

import com.launchwindow.dto.UserResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserQueryService {
    private final AppUserRepository repository;

    public UserQueryService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUser(String username) {
        return repository.findByUsername(username)
                .map(this::toResponse);
    }

    private UserResponse toResponse(AppUser user) {
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
