package com.buzzlink.repository;

import com.buzzlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Clerk ID (used for authentication mapping)
     */
    Optional<User> findByClerkId(String clerkId);

    /**
     * Check if a user exists by Clerk ID
     */
    boolean existsByClerkId(String clerkId);
}
