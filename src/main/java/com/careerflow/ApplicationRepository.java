package com.careerflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {

    List<ApplicationEntity> findByUserId(UUID userId);
    List<ApplicationEntity> findByUserIdAndStatus(UUID userId, com.careerflow.ApplicationStatus status);
    List<ApplicationEntity> findByUserIdAndCompanyContainingIgnoreCase(UUID userId, String company);
    List<ApplicationEntity> findByUserIdAndTitleContainingIgnoreCase(UUID userId, String title);
}
