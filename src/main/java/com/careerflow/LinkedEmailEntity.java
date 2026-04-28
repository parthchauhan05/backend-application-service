package com.careerflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "linked_emails",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "email"}))
public class LinkedEmailEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column
    private String label;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null)        id        = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    // --- Getters & Setters ---

    public UUID getId()                    { return id; }
    public void setId(UUID id)             { this.id = id; }

    public UUID getUserId()                { return userId; }
    public void setUserId(UUID userId)     { this.userId = userId; }

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getLabel()               { return label; }
    public void setLabel(String label)     { this.label = label; }

    public Instant getCreatedAt()          { return createdAt; }
    public void setCreatedAt(Instant t)    { this.createdAt = t; }
}