package com.chat.realtime_service.serdes;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.events.upstream.Session;
import com.chat.realtime_service.models.UserSessionActivity;
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


    public static Serde<UserSessionActivity> userSessionActivitySerde() {
        Serializer<UserSessionActivity> jsonSerializer = new JsonSerializer<>();
        Deserializer<UserSessionActivity> jsonDeserializer = new JsonDeserializer<>(UserSessionActivity.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }

    public static Serde<Session> sessionSerde() {
        Serializer<Session> jsonSerializer = new JsonSerializer<>();
        Deserializer<Session> jsonDeserializer = new JsonDeserializer<>(Session.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }

}
