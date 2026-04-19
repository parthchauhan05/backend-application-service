package com.careerflow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {

    // Legacy — kept for getSummary and other non-paginated internal uses
    List<ApplicationEntity> findByUserId(UUID userId);

    // Paginated: optional status + full-text search across title, company, location,
    // notes, recruiter_name, recruiter_email, source
    @Query("""
            SELECT a FROM ApplicationEntity a
            WHERE a.userId = :userId
              AND (:status IS NULL OR a.status = :status)
              AND (
                :search IS NULL OR :search = ''
                OR LOWER(a.title)          LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.company)        LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.location)       LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.notes)          LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.recruiterName)  LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.recruiterEmail) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.source)         LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY a.createdAt DESC
            """)
    Page<ApplicationEntity> findPagedByUserIdWithFilters(
            @Param("userId")  UUID userId,
            @Param("status")  ApplicationStatus status,
            @Param("search")  String search,
            Pageable pageable
    );
}
