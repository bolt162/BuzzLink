package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * MessageModeration entity for AI-powered content moderation.
 * Stores OpenAI moderation scores for messages.
 */
@Entity
@Table(name = "message_moderation", indexes = {
    @Index(name = "idx_message_flagged", columnList = "message_id,flagged"),
    @Index(name = "idx_workspace_flagged", columnList = "workspace_id,flagged,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Message being moderated
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /**
     * Workspace ID for faster filtering (denormalized)
     */
    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    /**
     * Overall score (1-5)
     * 1 = Safe, 5 = Highly problematic
     */
    @Column(nullable = false)
    private Integer overallScore = 1;

    /**
     * Sensitive content score (1-5)
     */
    @Column(nullable = false)
    private Integer sensitiveScore = 1;

    /**
     * Profanity/curse words score (1-5)
     */
    @Column(nullable = false)
    private Integer profanityScore = 1;

    /**
     * Hate speech/racist content score (1-5)
     */
    @Column(nullable = false)
    private Integer hateSpeechScore = 1;

    /**
     * Harassment/harmful content score (1-5)
     */
    @Column(nullable = false)
    private Integer harassmentScore = 1;

    /**
     * Illegal/dangerous content score (1-5)
     */
    @Column(nullable = false)
    private Integer illegalContentScore = 1;

    /**
     * Flagged if any score is > 3
     */
    @Column(nullable = false)
    private Boolean flagged = false;

    /**
     * Raw OpenAI moderation response (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    /**
     * Admin who reviewed this (nullable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private User reviewedByAdmin;

    /**
     * Review status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum ReviewStatus {
        PENDING,           // Not yet reviewed
        FALSE_POSITIVE,    // Admin marked as safe
        CONFIRMED          // Admin confirmed violation
    }
}
