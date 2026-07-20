package com.launchwindow.service.auth;

import com.launchwindow.dto.RegisterRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.UserAlreadyExistsException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import com.launchwindow.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        validateAvailability(username, email);

        String passwordHash = passwordEncoder.encode(request.password());

        AppUser user = repository.save(new AppUser(
                username,
                email,
                passwordHash,
                Role.USER
        ));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    private void validateAvailability(String username, String email) {
        if (repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already in use");
        }

        if (repository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }
}
