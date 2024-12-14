package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.events.upstream.NewMessageEvent;
import com.chat.realtime_service.events.upstream.UserSessionEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;

@Slf4j
public class Base64Utils {


    public static UserSessionEvent getUserSession(Event event){
        try {
            return parsePayloadBase64(event, UserSessionEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    public static NewMessageEvent getMessagePayload(Event event){
        try {
            return parsePayloadBase64(event, NewMessageEvent.class);
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
