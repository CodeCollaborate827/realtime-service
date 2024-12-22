package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.upstream.ConversationEvent;
import com.chat.realtime_service.events.upstream.MessageEvent;
import com.chat.realtime_service.events.upstream.NotificationEvent;
import com.chat.realtime_service.models.WebsocketMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageUtils {
    public static List<WebsocketMessage> createWebsocketMessageForEachMember(MessageEvent messageEvent) {
        List<WebsocketMessage> newMessageList = new ArrayList<>();

        // create each websocket message object for each member in the conversation for the message event
        for (String memberId : messageEvent.getConversationMemberIds()) {
            WebsocketMessage websocketMessage = WebsocketMessage.builder()
                    .data(messageEvent)
                    .wsMessageId(UUID.randomUUID().toString())
                    .userId(memberId)
                    .eventType(WebsocketMessage.EventType.MESSAGE_EVENT)
                    .timestamp(Instant.now().getEpochSecond())
                    .build();
            newMessageList.add(websocketMessage);
        }

        return newMessageList;
    }

    public static List<WebsocketMessage> createWebsocketMessageForEachMember(ConversationEvent conversationEvent) {
        List<WebsocketMessage> newMessageList = new ArrayList<>();

        // create each websocket message object for each member in the conversation for the conversation event
        for (String memberId : conversationEvent.getConversationMemberIds()) {
            WebsocketMessage websocketMessage = WebsocketMessage.builder()
                    .wsMessageId(UUID.randomUUID().toString()) // using the same id as in the new message event
                    .userId(memberId)
                    .data(conversationEvent)
                    .eventType(WebsocketMessage.EventType.CONVERSATION_EVENT)
                    .timestamp(Instant.now().getEpochSecond())
                    .build();
            newMessageList.add(websocketMessage);
        }

        return newMessageList;
    }


    public static WebsocketMessage convertToWebsocketMessage(NotificationEvent notificationEvent) {
        WebsocketMessage websocketMessage = WebsocketMessage.builder()
                .wsMessageId(UUID.randomUUID().toString())
                .eventType(WebsocketMessage.EventType.NOTIFICATION_EVENT)
                .userId(notificationEvent.getRecipientId())
                .timestamp(Instant.now().getEpochSecond())
                .data(notificationEvent)
                .build();

        return websocketMessage;
    }
}
