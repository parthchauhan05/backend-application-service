package com.careerflow;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserResolver {

    @PersistenceContext
    private EntityManager em;

    /**
     * Resolves a Supabase auth user UUID from their email address.
     * Returns null if no matching user exists.
     *
     * auth.users is Supabase's internal table — accessible via native queries
     * when using the postgres service role connection string.
     */
    public UUID resolveUserId(String email) {
        try {
            Object result = em.createNativeQuery(
                            "SELECT id FROM auth.users WHERE email = :email LIMIT 1"
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return UUID.fromString(result.toString());
        } catch (Exception e) {
            return null;
        }
    }
}