package com.nutrisync.service;

import com.nutrisync.domain.AppUser;
import com.nutrisync.domain.Role;
import com.nutrisync.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser registerClient(String name, String email, String password) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must contain 2–100 characters.");
        }
        if (email == null || email.trim().length() > 254 || !email.trim().matches("(?i)^[A-Z0-9.!#$%&'*+/=?^`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
        if (password == null || password.length() < 10 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain 10–72 characters.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with that email already exists.");
        }
        try {
            // Flush here so simultaneous registrations for the same unique email
            // become a deterministic conflict response rather than a later 500.
            return users.saveAndFlush(new AppUser(name.trim(), normalizedEmail, passwordEncoder.encode(password), Role.CLIENT));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "An account with that email already exists or conflicts with an existing record.");
        }
    }

    @Transactional
    public AppUser createUserIfMissing(String name, String email, String rawPassword, Role role) {
        return users.findByEmailIgnoreCase(email).orElseGet(() ->
            users.save(new AppUser(name, email, passwordEncoder.encode(rawPassword), role)));
    }
}
