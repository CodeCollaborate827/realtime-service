package com.chat.realtime_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// this message is used to send messages to the client via websocket
public class WebsocketMessage {

    public enum WebsocketMessageType {
        NEW_MESSAGE,
        NEW_MESSAGE_REACTION,
        NEW_CONVERSATION,
        CONVERSATION_NAME_UPDATED,
        CONVERSATION_MEMBER_ADDED,
        CONVERSATION_MEMBER_REMOVED,
        CONVERSATION_MEMBER_LEFT,
        NEW_NOTIFICATION,
    }

    private String userId;
    private String wsMessageId;
    private WebsocketMessageType type;
    private Object data;
}
