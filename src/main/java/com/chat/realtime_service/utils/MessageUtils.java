package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.upstream.NewMessageEvent;
import com.chat.realtime_service.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    public static List<ChatMessage> createMessageForEachMember(NewMessageEvent newMessageEvent) {
        List<ChatMessage> newMessageList = new ArrayList<>();

        // create each message object for each member in the group
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

            newMessageList.add(message);
        }

        return newMessageList;
    }
}
