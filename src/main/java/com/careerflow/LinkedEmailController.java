package com.careerflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/linked-emails")
public class LinkedEmailController {

    @Autowired
    private LinkedEmailRepository linkedEmailRepository;

    @Autowired
    private UserResolver userResolver;   // Step B4 — resolves UUID from email

    // ── GET /api/linked-emails ──────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());

        List<LinkedEmailEntity> rows =
                linkedEmailRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return ResponseEntity.ok(rows);
    }

    // ── POST /api/linked-emails ─────────────────────────────────────────
    record AddRequest(String email, String label) {}

    @PostMapping
    public ResponseEntity<?> add(Authentication auth,
                                 @RequestBody AddRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        // Basic format check — full validation happens on the DB UNIQUE constraint
        if (!req.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid email format"));
        }

        UUID userId = userResolver.resolveUserId(auth.getName());
        if (userId == null) return userNotFound();

        if (linkedEmailRepository.existsByUserIdAndEmailIgnoreCase(userId, req.email())) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "This email is already linked"));
        }

        LinkedEmailEntity entity = new LinkedEmailEntity();
        entity.setUserId(userId);
        entity.setEmail(req.email().toLowerCase().trim());
        entity.setLabel(req.label() != null ? req.label().trim() : null);

        LinkedEmailEntity saved = linkedEmailRepository.save(entity);
        return ResponseEntity.status(201).body(saved);
    }

    // ── DELETE /api/linked-emails/{id} ──────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(Authentication auth,
                                    @PathVariable UUID id) {
        UUID userId = userResolver.resolveUserId(auth.getName());
        if (userId == null) return userNotFound();

        boolean exists = linkedEmailRepository
                .findByIdAndUserId(id, userId).isPresent();
        if (!exists) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Email account not found"));
        }

        linkedEmailRepository.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();  // 204
    }

    private ResponseEntity<?> userNotFound() {
        return ResponseEntity.status(404)
                .body(Map.of("message", "User not found. Please log in again."));
    }
}