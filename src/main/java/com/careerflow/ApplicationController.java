package com.careerflow;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * GET /api/applications
     *
     * Query params:
     *   page   (int,    default 0)  — zero-based page index
     *   size   (int,    default 20) — records per page (max 100)
     *   status (string, optional)   — exact ApplicationStatus enum value
     *   search (string, optional)   — searched across title, company, location,
     *                                 notes, recruiterName, recruiterEmail, source
     *
     * Response: Spring Page<ApplicationEntity>
     *   {
     *     content:          [...],
     *     totalElements:    N,
     *     totalPages:       N,
     *     number:           0,       // current page (0-based)
     *     size:             20,
     *     first:            true,
     *     last:             false
     *   }
     */
    @GetMapping
    public ResponseEntity<Page<ApplicationEntity>> listMyApplication(
            Authentication authentication,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    ApplicationStatus status,
            @RequestParam(required = false)    String search
    ) {
        // Clamp page size to a safe maximum to avoid accidental full-table dumps
        int safeSize = Math.min(size, 100);

        UUID userId   = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, safeSize);

        // Treat blank search string the same as null so the JPQL short-circuit fires
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<ApplicationEntity> result = applicationRepository
                .findPagedByUserIdWithFilters(userId, status, normalizedSearch, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ApplicationEntity> create(
            Authentication authentication,
            @RequestBody @Valid ApplicationEntity application
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        application.setId(UUID.randomUUID());
        application.setUserId(userId);
        return ResponseEntity.ok(applicationRepository.save(application));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationEntity> update(
            @PathVariable UUID id,
            @RequestBody @Valid ApplicationEntity application
    ) {
        ApplicationEntity existing = applicationRepository.findById(id).orElseThrow();
        BeanUtils.copyProperties(application, existing, "id", "userId", "createdAt");
        return ResponseEntity.ok(applicationRepository.save(existing));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<ApplicationStatus, Long>> getSummary(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<ApplicationEntity> applications = applicationRepository.findByUserId(userId);
        Map<ApplicationStatus, Long> summary = applications.stream()
                .collect(Collectors.groupingBy(ApplicationEntity::getStatus, Collectors.counting()));
        return ResponseEntity.ok(summary);
    }
}
