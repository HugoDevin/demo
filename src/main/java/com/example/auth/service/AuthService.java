package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.EmailAlreadyExistsException;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.model.AppUser;
import com.example.auth.store.InMemoryUserStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final InMemoryUserStore userStore;
    private final PasswordEncoder passwordEncoder;

    public AuthService(InMemoryUserStore userStore, PasswordEncoder passwordEncoder) {
        this.userStore = userStore;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        AppUser user = new AppUser(normalizedEmail, encodedPassword);

        if (!userStore.save(user)) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        return "User registered successfully.";
    }

    public String login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        AppUser user = userStore.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getEncodedPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        return "Login successful";
    }
}
