package com.chat.realtime_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OldSession {
    private String sessionId;
    private String userId;
    private Long startTime;
    private Long endTime;
}
