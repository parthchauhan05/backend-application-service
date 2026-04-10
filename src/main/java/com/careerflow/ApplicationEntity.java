package com.careerflow;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="applications")
public class ApplicationEntity {

    @Id
    private UUID id;
    @Column(name="user_id")
    private UUID userId;
    private String title;
    private String company;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private String url;
    private String notes;
    @Column(name="created_at")
    private Instant createdAt;
    @Column(name="updated_at")
    private Instant updatedAt;
    private String location;
    private String jobType;
    private String source;
    @Column(name="recruiter_name")
    private String recruiterName;
    @Column(name="recruiter_email")
    private String recruiterEmail;
    @Column(name="applied_date")
    private Instant appliedDate;
    @Column(name="followup_date")
    private Instant followUpDate;
    @Column(name="interview_date")
    private Instant interviewDate;

    public ApplicationEntity() {}

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
