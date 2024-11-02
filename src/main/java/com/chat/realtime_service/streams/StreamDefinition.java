package com.chat.realtime_service.streams;

import com.chat.realtime_service.events.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafkaStreams
@Slf4j
public class StreamDefinition {

    @Value("${realtime.kafka.topic.user.session}")
    private String userSessionTopic;

    @Bean
    public KStream<String, Event> kStreamUserSessionStatus(StreamsBuilder streamsBuilder) {
        KStream<String, Event> stream = streamsBuilder.stream(userSessionTopic, Consumed.with(Serdes.String(), new JsonSerde<>(Event.class)))
                .peek((key, value) -> log.info("Key: {}, Value: {}", key, value));
//        Materialized.<String, UserOnlineStatus>as("user-online-status")
//                .withKeySerde(Serdes.String())
//                .withValueSerde(new JsonSerde<>(UserOnlineStatus.class));
        return stream;
    }
}
