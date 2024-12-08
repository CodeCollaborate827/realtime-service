package com.chat.realtime_service.serdes;

import com.chat.realtime_service.events.Event;
import com.chat.realtime_service.events.upstream.NewMessageEvent;
import com.chat.realtime_service.events.upstream.UserSessionEvent;
import com.chat.realtime_service.models.ChatMessage;
import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.models.WebsocketMessage;
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

    public static Serde<UserSessionEvent> sessionSerde() {
        Serializer<UserSessionEvent> jsonSerializer = new JsonSerializer<>();
        Deserializer<UserSessionEvent> jsonDeserializer = new JsonDeserializer<>(UserSessionEvent.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }

    public static Serde<NewMessageEvent> serdeNewMessageEvent() {
        Serializer<NewMessageEvent> serializer = new JsonSerializer<>();
        Deserializer<NewMessageEvent> deserializer = new JsonDeserializer<>(NewMessageEvent.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<ChatMessage> serdeChatMessage() {
        Serializer<ChatMessage> serializer = new JsonSerializer<>();
        Deserializer<ChatMessage> deserializer = new JsonDeserializer<>(ChatMessage.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<WebsocketMessage> serdeWebsocketMessage() {
        Serializer<WebsocketMessage> serializer = new JsonSerializer<>();
        Deserializer<WebsocketMessage> deserializer = new JsonDeserializer<>(WebsocketMessage.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

}
