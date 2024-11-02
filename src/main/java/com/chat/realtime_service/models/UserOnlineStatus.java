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
public class UserOnlineStatus {
    private String userId;
    private List<UserSession> currentSessions;
    private boolean isOnline;
    private List<UserSession> previousSessions; // TODO: keep only last 5 sessions
}
