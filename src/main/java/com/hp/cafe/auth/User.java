package com.hp.cafe.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    private String dateOfBirth;

    private String mobileNumber;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private String pushToken;

    protected User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getMobileNumber() { return mobileNumber; }
    public String getPushToken() { return pushToken; }

    public void updateProfile(String dateOfBirth, String mobileNumber) {
        this.dateOfBirth = dateOfBirth;
        this.mobileNumber = mobileNumber;
    }

    public void updatePushToken(String pushToken) { this.pushToken = pushToken; }
}
