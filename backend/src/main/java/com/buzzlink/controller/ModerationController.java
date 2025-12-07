package com.buzzlink.controller;

import com.buzzlink.dto.UserDTO;
import com.buzzlink.entity.MessageModeration;
import com.buzzlink.entity.User;
import com.buzzlink.repository.MessageModerationRepository;
import com.buzzlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ModerationController handles API endpoints for AI moderation
 */
@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@Slf4j
public class ModerationController {

    private final MessageModerationRepository moderationRepository;
    private final UserRepository userRepository;

    /**
     * Get flagged messages for a workspace (admin only)
     * GET /api/moderation/flagged?workspaceId={id}&limit=50
     */
    @GetMapping("/flagged")
    public ResponseEntity<?> getFlaggedMessages(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @RequestParam Long workspaceId,
            @RequestParam(defaultValue = "50") int limit) {

        // Verify user is admin
        User user = userRepository.findByClerkId(clerkId).orElse(null);
        if (user == null || !user.getIsAdmin()) {
            return ResponseEntity.status(403).body("Only admins can view flagged messages");
        }

        // Fetch flagged messages
        List<MessageModeration> flaggedMessages = moderationRepository.findFlaggedByWorkspaceId(
                workspaceId,
                PageRequest.of(0, limit)
        );

        // Convert to DTOs
        List<FlaggedMessageDTO> response = flaggedMessages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get moderation statistics for a workspace
     * GET /api/moderation/stats?workspaceId={id}
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getModerationStats(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @RequestParam Long workspaceId) {

        // Verify user is admin
        User user = userRepository.findByClerkId(clerkId).orElse(null);
        if (user == null || !user.getIsAdmin()) {
            return ResponseEntity.status(403).body("Only admins can view moderation stats");
        }

        long flaggedCount = moderationRepository.countByWorkspaceIdAndFlagged(workspaceId, true);

        return ResponseEntity.ok(new ModerationStatsDTO(flaggedCount));
    }

    /**
     * Mark a flagged message as reviewed (admin action)
     * POST /api/moderation/{moderationId}/review
     */
    @PostMapping("/{moderationId}/review")
    public ResponseEntity<?> reviewFlaggedMessage(
            @RequestHeader("X-Clerk-User-Id") String clerkId,
            @PathVariable Long moderationId,
            @RequestBody ReviewRequest request) {

        // Verify user is admin
        User admin = userRepository.findByClerkId(clerkId).orElse(null);
        if (admin == null || !admin.getIsAdmin()) {
            return ResponseEntity.status(403).body("Only admins can review messages");
        }

        // Find moderation record
        MessageModeration moderation = moderationRepository.findById(moderationId).orElse(null);
        if (moderation == null) {
            return ResponseEntity.notFound().build();
        }

        // Update review status
        moderation.setReviewStatus(MessageModeration.ReviewStatus.valueOf(request.status));
        moderation.setReviewedByAdmin(admin);
        moderationRepository.save(moderation);

        log.info("Admin {} reviewed moderation {} as {}", clerkId, moderationId, request.status);

        return ResponseEntity.ok(toDTO(moderation));
    }

    /**
     * Convert MessageModeration entity to DTO
     */
    private FlaggedMessageDTO toDTO(MessageModeration moderation) {
        return new FlaggedMessageDTO(
                moderation.getId(),
                moderation.getMessage().getId(),
                moderation.getMessage().getContent(),
                moderation.getMessage().getSender().getDisplayName(),
                moderation.getMessage().getSender().getClerkId(),
                moderation.getMessage().getChannel().getName(),
                moderation.getOverallScore(),
                moderation.getSensitiveScore(),
                moderation.getProfanityScore(),
                moderation.getHateSpeechScore(),
                moderation.getHarassmentScore(),
                moderation.getIllegalContentScore(),
                moderation.getCreatedAt(),
                moderation.getMessage().getCreatedAt(),
                moderation.getReviewStatus().name(),
                moderation.getReviewedByAdmin() != null ?
                    UserDTO.fromEntity(moderation.getReviewedByAdmin()) : null
        );
    }

    /**
     * DTO for flagged message response
     */
    public record FlaggedMessageDTO(
            Long moderationId,
            Long messageId,
            String messageContent,
            String senderName,
            String senderClerkId,
            String channelName,
            Integer overallScore,
            Integer sensitiveScore,
            Integer profanityScore,
            Integer hateSpeechScore,
            Integer harassmentScore,
            Integer illegalContentScore,
            LocalDateTime moderationCreatedAt,
            LocalDateTime messageCreatedAt,
            String reviewStatus,
            UserDTO reviewedBy
    ) {}

    /**
     * DTO for moderation statistics
     */
    public record ModerationStatsDTO(long flaggedCount) {}

    /**
     * Request body for reviewing a message
     */
    public record ReviewRequest(String status) {}
}
