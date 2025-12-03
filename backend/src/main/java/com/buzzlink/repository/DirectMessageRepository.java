package com.buzzlink.repository;

import com.buzzlink.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // Get conversation between two users
    @Query("SELECT dm FROM DirectMessage dm WHERE " +
           "(dm.sender.id = :userId1 AND dm.recipient.id = :userId2) OR " +
           "(dm.sender.id = :userId2 AND dm.recipient.id = :userId1) " +
           "ORDER BY dm.createdAt DESC")
    List<DirectMessage> findConversation(@Param("userId1") Long userId1,
                                         @Param("userId2") Long userId2,
                                         Pageable pageable);

    // Get all conversations for a user (distinct users they've messaged with)
    @Query("SELECT DISTINCT CASE WHEN dm.sender.id = :userId THEN dm.recipient ELSE dm.sender END " +
           "FROM DirectMessage dm WHERE dm.sender.id = :userId OR dm.recipient.id = :userId")
    List<com.buzzlink.entity.User> findConversationPartners(@Param("userId") Long userId);

    // Get recent DMs for a user
    @Query("SELECT dm FROM DirectMessage dm WHERE dm.sender.id = :userId OR dm.recipient.id = :userId " +
           "ORDER BY dm.createdAt DESC")
    List<DirectMessage> findRecentMessages(@Param("userId") Long userId, Pageable pageable);
}
