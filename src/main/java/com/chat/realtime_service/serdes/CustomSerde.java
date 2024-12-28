package com.chat.realtime_service.serdes;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.models.UserSessionHistory;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class CustomSerde {

    public static Serde<Event> eventSerde() {
        Serializer<Event> jsonSerializer = new JsonSerializer<>();
        Deserializer<Event> jsonDeserializer = new JsonDeserializer<>(Event.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }


    public static Serde<UserSessionHistory> userSessionActivitySerde() {
        Serializer<UserSessionHistory> jsonSerializer = new JsonSerializer<>();
        Deserializer<UserSessionHistory> jsonDeserializer = new JsonDeserializer<>(UserSessionHistory.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }


}
