package com.chat.realtime_service.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Class<T> destinationClass;

    public JsonDeserializer(Class<T> destinationClass) {
        this.destinationClass = destinationClass;
    }


    @Override
    public T deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, destinationClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
