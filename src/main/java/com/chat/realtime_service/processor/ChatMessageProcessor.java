package com.chat.realtime_service.processor;

import com.chat.realtime_service.models.ActiveSession;
import com.chat.realtime_service.models.ChatMessage;
import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.models.WebsocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChatMessageProcessor implements Processor<String, ChatMessage, String, List<WebsocketMessage>> {

    private String storeName;
    private ProcessorContext<String, List<WebsocketMessage>> context;
    private KeyValueStore<String, UserSessionActivity> store;

    public ChatMessageProcessor(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(ProcessorContext<String, List<WebsocketMessage>> context) {
        this.store = context.getStateStore(storeName);
        this.context = context;
    }

    @Override
    public void process(Record<String, ChatMessage> record) {
        ChatMessage chatMessage = record.value();
        String messageId = chatMessage.getMessageId();
        String userId = chatMessage.getRecipientId();


        if (checkUserIsOnline(store.get(userId))) {
            log.info("User is online, preparing message to user: {}", userId);
            // send message to user
            UserSessionActivity userSessionActivity = store.get(userId);
            // prepare message for each active session of user
            List<WebsocketMessage> websocketMessages = prepareWebsocketMessage(chatMessage, userSessionActivity);

            Record<String, List<WebsocketMessage>> recordToSend = new Record<>(userId, websocketMessages, record.timestamp(), record.headers());
            context.forward(recordToSend);
        } else {
            log.warn("User is offline, dropping message id: {} for user id: {}",messageId, userId);

        }
    }

    private List<WebsocketMessage> prepareWebsocketMessage(ChatMessage chatMessage, UserSessionActivity userSessionActivity) {
        List<WebsocketMessage> websocketMessages = new ArrayList<>();

        // each active session of user will receive the message
        for (ActiveSession activeSession : userSessionActivity.getActiveSessions()) {
            WebsocketMessage websocketMessage = WebsocketMessage.builder()
                    .sessionId(activeSession.getSessionId())
                    .type(WebsocketMessage.WebsocketMessageType.NEW_MESSAGE)
                    .data(chatMessage)
                    .build();
            websocketMessages.add(websocketMessage);
        }

        return websocketMessages;
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
