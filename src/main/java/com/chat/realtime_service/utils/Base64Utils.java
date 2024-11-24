package com.chat.realtime_service.utils;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.events.upstream.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;

@Slf4j
public class Base64Utils {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static Session getUserSession(Event event){
        try {
            return parsePayloadBase64(event, Session.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    public static <T> T parsePayloadBase64(Event event, Class<T> t) throws IOException {
        String originalString = event.getPayloadBase64();
        byte[] decodedBytes = Base64.getDecoder().decode(originalString);
        return objectMapper.readValue(decodedBytes, t);
    }

}
