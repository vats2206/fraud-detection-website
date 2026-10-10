package com.clearline.fraud.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clearline.fraud.api.ApiException;
import com.clearline.fraud.api.ApiDtos.RegisterRequest;
import com.clearline.fraud.api.ApiDtos.LoginRequest;
import com.clearline.fraud.model.AppUser;
import com.clearline.fraud.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists.");
        }
        AppUser user = new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()));
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public AppUser findById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
