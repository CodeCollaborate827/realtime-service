package com.chat.realtime_service.processor.supplier;

import com.chat.realtime_service.config.KafkaStreamsConfig;
import com.chat.realtime_service.events.upstream.UserSessionEvent;
import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.processor.UserSessionProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class UserSessionProcessorSupplier implements ProcessorSupplier<String, UserSessionEvent, String, UserSessionActivity> {

    private final RedisTemplate<String, UserSessionActivity> redisTemplate;

    @Override
    public Processor<String, UserSessionEvent, String, UserSessionActivity> get() {
        String storeName = KafkaStreamsConfig.USER_SESSION_ACTIVITY_STORE_NAME;
        return new UserSessionProcessor(storeName, redisTemplate);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return ProcessorSupplier.super.stores();
    }
}
