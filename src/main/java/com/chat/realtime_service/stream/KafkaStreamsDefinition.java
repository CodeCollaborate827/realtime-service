package com.chat.realtime_service.stream;

import com.chat.realtime_service.models.UserSessionHistory;
import com.chat.realtime_service.processor.supplier.WebsocketMessageProcessorSupplier;
import com.chat.realtime_service.processor.supplier.WebsocketSessionProcessorSupplier;
import com.chat.realtime_service.serdes.CustomSerde;
import com.chat.realtime_service.service.AmqpMessageSender;
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
public class KafkaStreamsDefinition {

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${realtime.kafka.topic.websocket.user.session}")
    private String websocketSessionTopic;

    @Value("${realtime.kafka.topic.messageEvents}")
    private String messageEventTopic; // upstream topic for message events (receive from messaging-service)

    @Value("${realtime.kafka.topic.conversationEvents}")
    private String conversationEventTopic; // upstream topic for conversation events (receive from messaging-service)

    @Value("${realtime.kafka.topic.notificationEvents}")
    private String notificationEventTopic; // upstream topic for notification events (receive from notification-manager)


    private final String USER_SESSION_ACTIVITY_PROCESSOR = "userSessionProcessor";
    private final String WEBSOCKET_MESSAGE_PROCESSOR = "chatMessageProcessor";

    private final WebsocketSessionProcessorSupplier websocketSessionProcessorSupplier;
    private final WebsocketMessageProcessorSupplier websocketMessageProcessorSupplier;
    private final AmqpMessageSender amqpMessageSender;

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

    private StoreBuilder<KeyValueStore<String, UserSessionHistory>> userSessionActivityReadOnlyKeyValueStore() {
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
        processMessageEvents(streamsBuilder);
        processConversationEvents(streamsBuilder);
        processNotificationEvents(streamsBuilder);

        return streamsBuilder;
    }



    private void processWebsocketSession(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(websocketSessionTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .peek((key, value) -> log.debug("WebsocketSessionEvent Key: {}, Value: {}", key, value))
                .mapValues(Base64Utils::getWebsocketSessionPayload)
                .filter((key, value) -> value != null)
                .selectKey((ignoreKey, value) -> value.getUserId())
                .process(websocketSessionProcessorSupplier, Named.as(USER_SESSION_ACTIVITY_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME);

    }

    private void processMessageEvents(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(messageEventTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .mapValues(Base64Utils::getMessageEventPayload)
                .peek((key, value) -> log.info("New Message Kafka, Key: {}, Value: {}", key, value))
                .flatMapValues(MessageUtils::createWebsocketMessageForEachMember)
                .selectKey((ignoreKey, value) -> value.getUserId())
                .process(websocketMessageProcessorSupplier, Named.as(WEBSOCKET_MESSAGE_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME)
                .peek(amqpMessageSender::sendMessageToUser);
    }

    private void processConversationEvents(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(conversationEventTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .peek((key, value) -> log.debug("ConversationEvent Key: {}, Value: {}", key, value))
                .mapValues(Base64Utils::getConversationEventPayload)
                .flatMapValues(MessageUtils::createWebsocketMessageForEachMember)
                .selectKey((ignoreKey, value) -> value.getUserId())
                .process(websocketMessageProcessorSupplier, Named.as(WEBSOCKET_MESSAGE_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME)
                .peek(amqpMessageSender::sendMessageToUser);
    }

    private void processNotificationEvents(StreamsBuilder streamsBuilder) {
        streamsBuilder.stream(notificationEventTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .peek((key, value) -> log.debug("NotificationEvent Key: {}, Value: {}", key, value))
                .mapValues(Base64Utils::getNotificationEventPayload)
                .mapValues(MessageUtils::convertToWebsocketMessage)
                .selectKey((ignoreKey, value) -> value.getUserId())
                .process(websocketMessageProcessorSupplier, Named.as(WEBSOCKET_MESSAGE_PROCESSOR), USER_SESSION_ACTIVITY_STORE_NAME)
                .peek(amqpMessageSender::sendMessageToUser);
    }

}
