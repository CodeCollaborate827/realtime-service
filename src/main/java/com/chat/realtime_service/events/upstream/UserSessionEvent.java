package com.chat.realtime_service.events.upstream;

import lombok.Data;

@Data
public class UserSessionEvent {
  private String status; // SESSION_START, SESSION_END
  private String userId;
  private String clientIp;
  private String sessionId;
  private long timestamp;
}
