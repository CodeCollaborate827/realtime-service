package com.chat.realtime_service.events.upstream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewMessageEvent {
  private String messageId;
  private String senderId;
  private String repliedMessageId;
  private String conversationId;
  private String messageContent;
  private Long messageCreatedAt;
  private List<String> conversationMemberIds;
}
