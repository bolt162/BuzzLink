package com.buzzlink.repository;

import com.buzzlink.entity.Message;
import com.buzzlink.entity.MessageModeration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageModerationRepository extends JpaRepository<MessageModeration, Long> {

    /**
     * Find moderation record by message
     */
    Optional<MessageModeration> findByMessage(Message message);

    /**
     * Find all flagged messages in a workspace for admin dashboard
     */
    @Query("SELECT mm FROM MessageModeration mm " +
           "JOIN FETCH mm.message m " +
           "JOIN FETCH m.sender " +
           "JOIN FETCH m.channel c " +
           "WHERE mm.workspaceId = :workspaceId AND mm.flagged = true " +
           "ORDER BY mm.createdAt DESC")
    List<MessageModeration> findFlaggedByWorkspaceId(Long workspaceId, Pageable pageable);

    /**
     * Count flagged messages in a workspace
     */
    long countByWorkspaceIdAndFlagged(Long workspaceId, boolean flagged);

    /**
     * Find all flagged messages pending review
     */
    @Query("SELECT mm FROM MessageModeration mm " +
           "WHERE mm.workspaceId = :workspaceId " +
           "AND mm.flagged = true " +
           "AND mm.reviewStatus = 'PENDING' " +
           "ORDER BY mm.createdAt DESC")
    List<MessageModeration> findPendingReviewByWorkspaceId(Long workspaceId, Pageable pageable);
}
