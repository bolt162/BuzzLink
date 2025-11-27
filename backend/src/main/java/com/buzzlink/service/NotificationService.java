package com.buzzlink.service;

import com.buzzlink.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Notification service for publishing events to Kafka
 *
 * DESIGN ONLY - This is a stub implementation for the demo.
 * In a production system, this would:
 * 1. Use KafkaTemplate to publish events to Kafka topics
 * 2. Have separate topics for different event types (messages, mentions, reactions)
 * 3. Include retry logic and error handling
 * 4. Serialize events to JSON or Avro
 *
 * Example Kafka topics that would exist:
 * - buzzlink.messages.new - New message events
 * - buzzlink.messages.deleted - Message deletion events
 * - buzzlink.mentions - User mention events
 * - buzzlink.reactions - Reaction events
 *
 * Consumers of these topics would include:
 * - Email notification service
 * - Push notification service
 * - Analytics pipeline
 * - Search indexing service
 */
@Service
@Slf4j
public class NotificationService {

    // Uncomment in production when Kafka is fully configured:
    // private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish a notification when a new message is created
     * In production, this would send to Kafka topic: buzzlink.messages.new
     */
    public void publishMessageNotification(Message message) {
        log.info("KAFKA STUB: Would publish message notification to Kafka - Message ID: {}, Channel: {}, Sender: {}",
            message.getId(),
            message.getChannel().getName(),
            message.getSender().getDisplayName());

        /*
         * Production implementation would look like:
         *
         * MessageEvent event = new MessageEvent(
         *     message.getId(),
         *     message.getChannel().getId(),
         *     message.getSender().getClerkId(),
         *     message.getContent(),
         *     message.getCreatedAt()
         * );
         *
         * kafkaTemplate.send("buzzlink.messages.new",
         *     message.getChannel().getId().toString(),
         *     event);
         */
    }

    /**
     * Publish a notification when a message is deleted
     * In production, this would send to Kafka topic: buzzlink.messages.deleted
     */
    public void publishMessageDeletedNotification(Long messageId, Long channelId) {
        log.info("KAFKA STUB: Would publish delete notification to Kafka - Message ID: {}, Channel ID: {}",
            messageId, channelId);
    }

    /**
     * Publish a notification when a user is mentioned
     * In production, this would send to Kafka topic: buzzlink.mentions
     */
    public void publishMentionNotification(String mentionedUserClerkId, Message message) {
        log.info("KAFKA STUB: Would publish mention notification to Kafka - Mentioned user: {}, Message ID: {}",
            mentionedUserClerkId, message.getId());
    }
}
