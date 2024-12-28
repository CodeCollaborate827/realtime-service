package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.events.upstream.ConversationEvent;
import com.chat.realtime_service.events.upstream.MessageEvent;
import com.chat.realtime_service.events.upstream.NotificationEvent;
import com.chat.realtime_service.events.upstream.WebsocketSessionEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;

@Slf4j
public class Base64Utils {


    public static WebsocketSessionEvent getWebsocketSessionPayload(Event event){
        try {
            return parsePayloadBase64(event, WebsocketSessionEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    public static MessageEvent getMessageEventPayload(Event event){
        try {
            return parsePayloadBase64(event, MessageEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    public static ConversationEvent getConversationEventPayload(Event event) {
        try {
            return parsePayloadBase64(event, ConversationEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static NotificationEvent getNotificationEventPayload(Event event) {
        try {
            return parsePayloadBase64(event, NotificationEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static <T> T parsePayloadBase64(Event event, Class<T> t) throws IOException {
        String originalString = event.getPayloadBase64();
        byte[] decodedBytes = Base64.getDecoder().decode(originalString);
        return JsonUtils.toObject(decodedBytes, t);
    }

}
