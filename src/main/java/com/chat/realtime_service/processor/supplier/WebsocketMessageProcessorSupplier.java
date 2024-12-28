package com.chat.realtime_service.processor.supplier;

import com.chat.realtime_service.models.WebsocketMessage;
import com.chat.realtime_service.processor.WebsocketMessageProcessor;
import com.chat.realtime_service.stream.KafkaStreamsDefinition;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

@Component
public class WebsocketMessageProcessorSupplier implements ProcessorSupplier<String, WebsocketMessage, String, WebsocketMessage> {
    @Override
    public Processor<String, WebsocketMessage, String, WebsocketMessage> get() {
        String storeName = KafkaStreamsDefinition.USER_SESSION_ACTIVITY_STORE_NAME;
        return new WebsocketMessageProcessor(storeName);
    }
}
