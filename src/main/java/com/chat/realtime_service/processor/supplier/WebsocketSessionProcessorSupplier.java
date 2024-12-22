package com.chat.realtime_service.processor.supplier;

import com.chat.realtime_service.events.upstream.WebsocketSessionEvent;
import com.chat.realtime_service.models.UserSessionHistory;
import com.chat.realtime_service.processor.WebsocketSessionProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class WebsocketSessionProcessorSupplier implements ProcessorSupplier<String, WebsocketSessionEvent, String, UserSessionHistory> {

    private final RedisTemplate<String, UserSessionHistory> redisTemplate;

    @Override
    public Processor<String, WebsocketSessionEvent, String, UserSessionHistory> get() {
        String storeName = KafkaStreamsConfig.USER_SESSION_ACTIVITY_STORE_NAME;
        return new WebsocketSessionProcessor(storeName, redisTemplate);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return ProcessorSupplier.super.stores();
    }
}
