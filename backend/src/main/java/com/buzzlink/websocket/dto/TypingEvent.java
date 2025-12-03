package com.buzzlink.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket message for typing indicators
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    private Long channelId;
    private String clerkId;
    private String displayName;
    private boolean isTyping;
}
