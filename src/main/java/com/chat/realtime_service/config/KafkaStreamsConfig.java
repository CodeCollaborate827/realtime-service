package com.chat.realtime_service.config;

import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.processor.supplier.ChatMessageProcessorSupplier;
import com.chat.realtime_service.processor.supplier.UserSessionProcessorSupplier;
import com.chat.realtime_service.serdes.CustomSerde;
import com.chat.realtime_service.utils.Base64Utils;
import com.chat.realtime_service.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Properties;

@Configuration
@Slf4j
@EnableKafkaStreams
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${realtime.kafka.topic.websocket.user.session}")
    private String websocketSessionTopic;

    @Value("${realtime.kafka.topic.message.new}")
    private String newMessageTopic; // upstream topic for new message (receive from message service)

    @Value("${realtime.kafka.topic.websocket.message}")
    private String websocketMessageTopic; // downstream topic for new message (send to WS-hub)

    private final String USER_SESSION_ACTIVITY_PROCESSOR = "userSessionProcessor";
    private final String CHAT_MESSAGE_PROCESSOR = "chatMessageProcessor";

    private final UserSessionProcessorSupplier userSessionProcessorSupplier;
    private final ChatMessageProcessorSupplier chatMessageProcessorSupplier;

    public static final String USER_SESSION_ACTIVITY_STORE_NAME = "USER_SESSION_ACTIVITY_STORE";


    @Bean
    public StreamsConfig streamsConfig() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/realtime-service" + applicationId);

        return new StreamsConfig(properties);
    }

    // STORE CREATION

    private StoreBuilder<KeyValueStore<String, UserSessionActivity>> userSessionActivityReadOnlyKeyValueStore() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(USER_SESSION_ACTIVITY_STORE_NAME),
                Serdes.String(),
                CustomSerde.userSessionActivitySerde()
        );

    }

    // STREAMS BUILDER

    @Bean
    public StreamsBuilder streamsBuilder(StreamsBuilder streamsBuilder) {
        streamsBuilder.addStateStore(userSessionActivityReadOnlyKeyValueStore());

        processWebsocketSession(streamsBuilder);
        processNewMessage(streamsBuilder);

        return streamsBuilder;
    }


    private void processWebsocketSession(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(websocketSessionTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .peek((key, value) -> log.debug("UserSessionEvent Key: {}, Value: {}", key, value))
                .mapValues(Base64Utils::getUserSession)
                .filter((key, value) -> value != null)
                .process(userSessionProcessorSupplier, Named.as(USER_SESSION_ACTIVITY_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME);

    }

    private void processNewMessage(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(newMessageTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .mapValues(Base64Utils::getMessagePayload)
                .peek((key, value) -> log.info("New Message Kafka, Key: {}, Value: {}", key, value))
                .flatMapValues(newMessageEVent -> MessageUtils.createMessageForEachMember(newMessageEVent))
                .process(chatMessageProcessorSupplier, Named.as(CHAT_MESSAGE_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME)
                .flatMapValues(websocketMessages -> websocketMessages)
                .selectKey((ignoredKey, websocketMessage) -> websocketMessage.getSessionId()) // the key is the session id
                .to(websocketMessageTopic, Produced.with(Serdes.String(), CustomSerde.serdeWebsocketMessage()));
                // TODO: send to RabbitMQ not Kafka
    }
}
