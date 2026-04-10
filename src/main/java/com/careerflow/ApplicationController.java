package com.careerflow;

import jakarta.validation.Valid;
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

    @GetMapping
    public ResponseEntity<List<ApplicationEntity>> listMyApplication(
            Authentication authentication,
            @RequestParam(required = false) com.careerflow.ApplicationStatus status,
            @RequestParam(required = false) String  company,
            @RequestParam(required = false) String title
            ) {
        UUID userId = UUID.fromString(authentication.getName());

        if(status != null) {
            return ResponseEntity.ok(applicationRepository.findByUserIdAndStatus(userId, status));
        }
        if(company != null && !company.isBlank()) {
            return ResponseEntity.ok(applicationRepository.findByUserIdAndCompanyContainingIgnoreCase(userId, company));
        }
        if(title != null && !title.isBlank()) {
            return ResponseEntity.ok(applicationRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title));
        }

        return ResponseEntity.ok(applicationRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ApplicationEntity> create(Authentication authentication, @RequestBody @Valid ApplicationEntity application) {
        UUID userId = UUID.fromString(authentication.getName());
        application.setId(UUID.randomUUID());
        application.setUserId(userId);
        ApplicationEntity savedApplication = applicationRepository.save(application);
        return  ResponseEntity.ok(savedApplication);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationEntity> update(@PathVariable UUID id, @RequestBody @Valid ApplicationEntity application) {
        ApplicationEntity existingApplication = applicationRepository.findById(id).orElseThrow();
        BeanUtils.copyProperties(application,existingApplication, "id", "userId", "createdAt");
        ApplicationEntity updatedApplication = applicationRepository.save(existingApplication);
        return ResponseEntity.ok(updatedApplication);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<ApplicationStatus, Long>> getSummary(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<ApplicationEntity> applications = applicationRepository.findByUserId(userId);
        Map<ApplicationStatus, Long> summary = applications.stream().collect(Collectors.groupingBy(ApplicationEntity::getStatus, Collectors.counting()));
        return ResponseEntity.ok(summary);
    }
}
