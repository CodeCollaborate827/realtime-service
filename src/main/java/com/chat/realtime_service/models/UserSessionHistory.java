package com.chat.realtime_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionHistory {
    private String userId;
    private List<ActiveSession> activeSessions;
    private List<OldSession> previousSessions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveSession {
        private String sessionId;
        private String clientIp;
        private String userId;
        private Long startTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OldSession {
        private String sessionId;
        private String userId;
        private String clientIp;
        private Long startTime;
        private Long endTime;
    }
}
