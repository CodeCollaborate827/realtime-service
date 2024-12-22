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
public class MessageEvent {
  public enum MessageEventType {
    MESSAGE_NEW,
    MESSAGE_DELIVERED,
    MESSAGE_SENT,
    MESSAGE_REACTED,
    MESSAGE_EDITED,
    MESSAGE_DELETED
  }

  private MessageEventType messageType;
  private String messageId;
  private String conversationId;
  private Object data; // depends on the messageType the data would be different
  private List<String> conversationMemberIds;
}
