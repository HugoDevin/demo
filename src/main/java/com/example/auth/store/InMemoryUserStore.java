package com.example.auth.store;

import com.example.auth.model.AppUser;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUserStore {

    private final ConcurrentHashMap<String, AppUser> users = new ConcurrentHashMap<>();

    public Optional<AppUser> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }

    public boolean existsByEmail(String email) {
        return users.containsKey(email);
    }

    public boolean save(AppUser user) {
        return users.putIfAbsent(user.getEmail(), user) == null;
    }
}
