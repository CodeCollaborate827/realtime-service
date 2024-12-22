package com.chat.realtime_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String repliedMessageId;
    private String conversationId;
    private String messageContent;
    private Long messageCreatedAt;
}
