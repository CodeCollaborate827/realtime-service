package com.chat.realtime_service.processor;

import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.models.WebsocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class ChatMessageProcessor implements Processor<String, WebsocketMessage, String, WebsocketMessage> {

    private String storeName;
    private ProcessorContext<String, WebsocketMessage> context;
    private KeyValueStore<String, UserSessionActivity> store;

    public ChatMessageProcessor(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(ProcessorContext<String, WebsocketMessage> context) {
        this.store = context.getStateStore(storeName);
        this.context = context;
    }

    @Override
    public void process(Record<String, WebsocketMessage> record) {
        WebsocketMessage chatMessage = record.value();
        String userId = chatMessage.getUserId();
        String wsMessageId = chatMessage.getWsMessageId();

        if (checkUserIsOnline(store.get(userId))) {
            log.info("User is online, sending message to user's websocket: {}", userId);
            Record<String, WebsocketMessage> recordToSend = new Record<>(userId, chatMessage, record.timestamp(), record.headers());
            context.forward(recordToSend);
        } else {
            log.warn("User is offline, dropping message id: {} for user id: {}",wsMessageId, userId);

        }
    }



    private boolean checkUserIsOnline(UserSessionActivity userSessionActivity) {
        return userSessionActivity != null &&
                userSessionActivity.getActiveSessions() != null &&
                !userSessionActivity.getActiveSessions().isEmpty();
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
