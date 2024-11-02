package com.chat.realtime_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
  // TODO: this one is duplicated across services

  @Builder.Default private String specVersion = "1.0";
  private String type;
  @Builder.Default private String id = UUID.randomUUID().toString();
  @Builder.Default private Long timestamp = Instant.now().getEpochSecond();
  @Builder.Default private String dataContentType = "application/json";
  private String payloadBase64;
}
