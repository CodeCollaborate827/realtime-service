package com.chat.realtime_service.events.upstream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    public enum NotificationEventType {
        NOTIFICATION_MESSAGE_REACTED,
        NOTIFICATION_MESSAGE_MENTIONED,
        NOTIFICATION_NEW_FRIEND_REQUEST,
        NOTIFICATION_FRIEND_REQUEST_ACCEPTED
    }

    private String notificationId;
    private NotificationEventType messageType;
    private String recipientId;
    private Object data;
}
