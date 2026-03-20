package com.example.auth.model;

public class AppUser {

    private final String email;
    private final String encodedPassword;

    public AppUser(String email, String encodedPassword) {
        this.email = email;
        this.encodedPassword = encodedPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }
}
