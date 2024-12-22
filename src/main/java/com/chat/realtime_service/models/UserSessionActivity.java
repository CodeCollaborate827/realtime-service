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
public class UserSessionActivity {
    private String userId;
    private List<ActiveSession> activeSessions;
    private List<OldSession> previousSessions;// TODO: keep only last 5 sessions
}
