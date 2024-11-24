package com.chat.realtime_service.config;

import com.chat.realtime_service.aggregator.UserSessionStatusAggregator;
import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.serdes.CustomSerde;
import com.chat.realtime_service.utils.Base64Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Properties;

import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;

@Configuration
@Slf4j
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${realtime.kafka.topic.user.session}")
    private String userSessionTopic;
    public static final String USER_SESSION_ACTIVITY_STORE = "USER_SESSION_ACTIVITY_STORE";


    @Bean
    public StreamsConfig streamsConfig() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/realtime-service" + applicationId);

        return new StreamsConfig(properties);
    }

    public StoreBuilder<KeyValueStore<String, UserSessionActivity>> getSubscribeBufferStateStore(){
        return keyValueStoreBuilder(
                Stores.persistentKeyValueStore(USER_SESSION_ACTIVITY_STORE),
                Serdes.String(),
                CustomSerde.userSessionActivitySerde()
        );

    }

//    @Bean
//    public StreamsBuilder streamsBuilder(StreamsBuilder streamsBuilder) {
////        KTable<String, UserSessionActivity> stringUserSessionActivityKTable = kStreamUserSessionStatus(streamsBuilder);
//        return streamsBuilder;
//    }

    @Bean
    public UserSessionStatusAggregator userSessionStatusAggregator(RedisTemplate<String, UserSessionActivity> redisTemplate) {
        return new UserSessionStatusAggregator(redisTemplate);
    }

    @Bean
    public KTable<String, UserSessionActivity> kStreamUserSessionStatus(StreamsBuilder streamsBuilder, RedisTemplate<String, UserSessionActivity> redisTemplate) {
        return streamsBuilder.stream(userSessionTopic, Consumed.with(Serdes.String(), CustomSerde.eventSerde()))
                .peek((key, value) -> log.info("Key: {}, Value: {}", key, value))
                .mapValues(Base64Utils::getUserSession)
                .filter((key, value) -> value != null)
                .selectKey((key, value) -> value.getUserId())
                .groupByKey(Grouped.with(Serdes.String(), CustomSerde.sessionSerde()))
                .aggregate(
                        UserSessionActivity::new,
                        userSessionStatusAggregator(redisTemplate),
                        Materialized.<String, UserSessionActivity, KeyValueStore<Bytes, byte[]>>as(USER_SESSION_ACTIVITY_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(CustomSerde.userSessionActivitySerde())
                );
    }
}
