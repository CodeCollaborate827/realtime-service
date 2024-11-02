package com.chat.realtime_service.events.upstream;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserSessionStatus {
  private String status; // SESSION_START, SESSION_END
  private String userId;
  private String sessionId;
  private OffsetDateTime createdAt;
}
