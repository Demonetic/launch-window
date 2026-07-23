package com.launchwindow.service.auth;

import com.launchwindow.config.JwtProperties;
import com.launchwindow.dto.LoginRequest;
import com.launchwindow.dto.LoginResponse;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginService(AppUserRepository repository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, JwtProperties jwtProperties) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        String normalizedEmail = identifier.toLowerCase(Locale.ROOT);

        AppUser user = repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.createToken(user);

        return new LoginResponse(token, "Bearer", jwtProperties.expiration().toSeconds(), toResponse(user));
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
