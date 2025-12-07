package com.buzzlink.service;

import com.buzzlink.entity.Message;
import com.buzzlink.entity.MessageModeration;
import com.buzzlink.repository.MessageModerationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * ModerationService handles AI-powered message moderation using OpenAI API
 */
@Service
@Slf4j
public class ModerationService {

    private final MessageModerationRepository moderationRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.moderation.enabled:true}")
    private boolean moderationEnabled;

    private static final String OPENAI_MODERATION_URL = "https://api.openai.com/v1/moderations";

    public ModerationService(MessageModerationRepository moderationRepository) {
        this.moderationRepository = moderationRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyze message content asynchronously using OpenAI Moderation API
     */
    @Async
    @Transactional
    public void analyzeMessage(Message message, Long workspaceId) {
        if (!moderationEnabled) {
            log.debug("Moderation is disabled, skipping message {}", message.getId());
            return;
        }

        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            log.warn("OpenAI API key not configured, skipping moderation for message {}", message.getId());
            return;
        }

        try {
            log.info("Analyzing message {} for moderation", message.getId());

            // Call OpenAI Moderation API
            String responseBody = callOpenAIModerationAPI(message.getContent());

            // Parse response
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode result = root.path("results").get(0);

            // Extract category scores
            JsonNode categoryScores = result.path("category_scores");

            // Map OpenAI scores (0.0-1.0) to our scale (1-5)
            int hateSpeechScore = mapScore(categoryScores.path("hate").asDouble());
            int harassmentScore = mapScore(categoryScores.path("harassment").asDouble());
            int profanityScore = mapScore(categoryScores.path("sexual").asDouble()); // Using sexual as proxy for profanity
            int sensitiveScore = mapScore(categoryScores.path("self-harm").asDouble());
            int illegalContentScore = mapScore(categoryScores.path("violence").asDouble());

            // Calculate overall score (max of all scores)
            int overallScore = Math.max(hateSpeechScore,
                    Math.max(harassmentScore,
                            Math.max(profanityScore,
                                    Math.max(sensitiveScore, illegalContentScore))));

            // Determine if message should be flagged (any score > 3)
            boolean flagged = overallScore > 3;

            // Create and save moderation record
            MessageModeration moderation = new MessageModeration();
            moderation.setMessage(message);
            moderation.setWorkspaceId(workspaceId);
            moderation.setOverallScore(overallScore);
            moderation.setSensitiveScore(sensitiveScore);
            moderation.setProfanityScore(profanityScore);
            moderation.setHateSpeechScore(hateSpeechScore);
            moderation.setHarassmentScore(harassmentScore);
            moderation.setIllegalContentScore(illegalContentScore);
            moderation.setFlagged(flagged);
            moderation.setRawResponse(responseBody);

            moderationRepository.save(moderation);

            if (flagged) {
                log.warn("Message {} flagged with overall score {}", message.getId(), overallScore);
            } else {
                log.info("Message {} passed moderation with overall score {}", message.getId(), overallScore);
            }

        } catch (Exception e) {
            log.error("Error analyzing message {} for moderation", message.getId(), e);
        }
    }

    /**
     * Call OpenAI Moderation API
     */
    private String callOpenAIModerationAPI(String content) throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new ModerationRequest(content)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_MODERATION_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI API returned status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    /**
     * Map OpenAI score (0.0-1.0) to our scale (1-5)
     */
    private int mapScore(double openaiScore) {
        if (openaiScore < 0.1) return 1;  // Very safe
        if (openaiScore < 0.3) return 2;  // Mostly safe
        if (openaiScore < 0.5) return 3;  // Borderline
        if (openaiScore < 0.8) return 4;  // Problematic
        return 5;  // Highly problematic
    }

    /**
     * Request DTO for OpenAI Moderation API
     */
    private record ModerationRequest(String input) {}
}
