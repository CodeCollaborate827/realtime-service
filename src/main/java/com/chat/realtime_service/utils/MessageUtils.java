package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.upstream.NewMessageEvent;
import com.chat.realtime_service.models.ChatMessage;
import com.chat.realtime_service.models.WebsocketMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    public static List<WebsocketMessage> createMessageForEachMember(NewMessageEvent newMessageEvent) {
        List<WebsocketMessage> newMessageList = new ArrayList<>();

        // create each websocket message object for each member in the group
        for (String memberId : newMessageEvent.getConversationMemberIds()) {
            ChatMessage message = ChatMessage.builder()
                    .messageCreatedAt(newMessageEvent.getMessageCreatedAt())
                    .messageContent(newMessageEvent.getMessageContent())
                    .repliedMessageId(newMessageEvent.getRepliedMessageId())
                    .messageId(newMessageEvent.getMessageId())
                    .conversationId(newMessageEvent.getConversationId())
                    .senderId(newMessageEvent.getSenderId())
                    .recipientId(memberId)
                    .build();

            WebsocketMessage websocketMessage = WebsocketMessage.builder()
                    .data(message)
                    .wsMessageId(newMessageEvent.getMessageId()) // using the same id as in the new message event
                    .userId(memberId)
                    .type(WebsocketMessage.WebsocketMessageType.NEW_MESSAGE)
                    .build();
            newMessageList.add(websocketMessage);
        }

        return newMessageList;
    }
}
