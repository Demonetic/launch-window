package com.launchwindow.service.user;

import com.launchwindow.dto.DeleteAccountRequest;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDeletionService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserDeletionService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void deleteAccount(String username, DeleteAccountRequest request) {
        AppUser user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Password is incorrect");
        }

        repository.delete(user);
    }
}