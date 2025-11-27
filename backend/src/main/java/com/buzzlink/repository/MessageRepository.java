package com.buzzlink.repository;

import com.buzzlink.entity.Channel;
import com.buzzlink.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find recent messages in a channel, ordered by creation time descending
     * Used for initial message history load
     */
    List<Message> findByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);

    /**
     * Find messages by channel with sender info for BI analytics
     * This query would be useful for Apache Superset dashboards
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.channel = :channel ORDER BY m.createdAt DESC")
    List<Message> findByChannelWithSender(Channel channel, Pageable pageable);

    /**
     * Count messages in a channel (useful for analytics)
     */
    long countByChannel(Channel channel);
}
