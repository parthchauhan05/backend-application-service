package com.careerflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkedEmailRepository extends JpaRepository<LinkedEmailEntity, UUID> {

    List<LinkedEmailEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<LinkedEmailEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndEmailIgnoreCase(UUID userId, String email);

    void deleteByIdAndUserId(UUID id, UUID userId);
}