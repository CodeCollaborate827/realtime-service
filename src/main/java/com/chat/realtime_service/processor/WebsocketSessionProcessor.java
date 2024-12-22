package com.chat.realtime_service.processor;

import com.chat.realtime_service.events.upstream.WebsocketSessionEvent;
import com.chat.realtime_service.models.UserSessionHistory;
import com.chat.realtime_service.models.UserSessionHistory.ActiveSession;
import com.chat.realtime_service.models.UserSessionHistory.OldSession;
import com.chat.realtime_service.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.chat.realtime_service.constants.ApplicationConstants.SESSION_END;
import static com.chat.realtime_service.constants.ApplicationConstants.SESSION_START;

@Slf4j
public class WebsocketSessionProcessor implements Processor<String, WebsocketSessionEvent, String, UserSessionHistory> {
    private static final int MAX_OLD_SESSIONS_LIMIT = 5;

    private String storeName;
    private ProcessorContext<String, UserSessionHistory> context;
    private KeyValueStore<String, UserSessionHistory> store;
    private RedisTemplate<String, UserSessionHistory> redisTemplate;


    public WebsocketSessionProcessor(String storeName, RedisTemplate<String, UserSessionHistory> redisTemplate) {
        this.storeName = storeName;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void init(ProcessorContext<String, UserSessionHistory> context) {
        this.context = context;
        this.store = context.getStateStore(storeName);
    }

    @Override
    public void process(Record<String, WebsocketSessionEvent> record) {

        String key = record.key();
        WebsocketSessionEvent websocketSessionEvent = record.value();

        String userId = websocketSessionEvent.getUserId();

        UserSessionHistory userSessionHistory = store.get(userId);
        // init if not exists
        if (userSessionHistory == null) {
            log.info("Init user session activity for user id: {}", userId);
            userSessionHistory = UserSessionHistory.builder()
                    .activeSessions(new ArrayList<>())
                    .previousSessions(new ArrayList<>())
                    .userId(userId)
                    .build();
        }

        String sessionStatus = websocketSessionEvent.getStatus();
        String sessionId = websocketSessionEvent.getSessionId();

        if (!SESSION_START.equals(sessionStatus) && !SESSION_END.equals(sessionStatus)) {
            log.error("Invalid status of session: {} for session id: {}, userId: {}", sessionStatus, sessionId, userId);
            return; // drop the message
        }

        // process the message based on the session status
        if (SESSION_START.equals(sessionStatus)) {
            log.info("Adding new active session id: {} for user: {}", sessionId, userId);
            addCurrentSession(userSessionHistory, websocketSessionEvent);
        } else if (SESSION_END.equals(sessionStatus)) {
            log.info("Removing active session id: {} for user: {}", sessionId, userId);
            removeCurrentSessionAndAddPreviousSession(userSessionHistory, websocketSessionEvent);
        }

        // store the updated user session activity to the local store
        store.put(userId, userSessionHistory);
        // store the updated user session activity for querying
        storeUserSessionActivityToRedis(userSessionHistory);

        // forward the new record to downstream
        Record<String, UserSessionHistory> newRecord = new Record<>(userId, userSessionHistory, record.timestamp(), record.headers());
        context.forward(newRecord);
    }

    private void addCurrentSession(UserSessionHistory userSessionHistory, WebsocketSessionEvent sessionStatusEvent) {

        boolean alreadyExists = userSessionHistory.getActiveSessions().stream().anyMatch(session -> session.getSessionId().equals(sessionStatusEvent.getSessionId()));

        if (alreadyExists) {
            log.warn("User session id: {} already exists as current active session for user id: {}", sessionStatusEvent.getSessionId(), userSessionHistory.getUserId());
            return;
        }

        ActiveSession userCurrentSession = ActiveSession.builder()
                .sessionId(sessionStatusEvent.getSessionId())
                .userId(sessionStatusEvent.getUserId())
                .startTime(sessionStatusEvent.getTimestamp())
                .clientIp(sessionStatusEvent.getClientIp())
                .build();

        userSessionHistory.getActiveSessions().add(userCurrentSession);

    }

    private void removeCurrentSessionAndAddPreviousSession(UserSessionHistory userSessionHistory, WebsocketSessionEvent sessionStatusEvent) {
        String sessionId = sessionStatusEvent.getSessionId();

        // get the session with that id, remove from currentSession list and add it to previousSession list
        Optional<ActiveSession> session = userSessionHistory.getActiveSessions()
                .stream()
                .filter(currentSession -> currentSession.getSessionId().equals(sessionId))
                .findFirst();

        if (session.isEmpty()) {
            log.error("User session id: {} does not existing as current active session for user id: {}", sessionId, userSessionHistory.getUserId());
            return;
        }
        userSessionHistory.getActiveSessions().removeIf(currentSession -> currentSession.getSessionId().equals(sessionId));

        log.info("SESSION: {}", session.get());
        OldSession oldSession = OldSession.builder()
                .sessionId(sessionStatusEvent.getSessionId())
                .userId(sessionStatusEvent.getUserId())
                .startTime(session.get().getStartTime())
                .clientIp(sessionStatusEvent.getClientIp())
                .endTime(sessionStatusEvent.getTimestamp())
                .build();

        userSessionHistory.getPreviousSessions().addFirst(oldSession); // sort by latest
        cleanUpOldSession(userSessionHistory);


    }

    private static void cleanUpOldSession(UserSessionHistory userSessionHistory) {
        List<OldSession> previousSessions = userSessionHistory.getPreviousSessions();
        while (previousSessions.size() > MAX_OLD_SESSIONS_LIMIT) {
            userSessionHistory.getPreviousSessions().removeLast();
        }
    }

    private void storeUserSessionActivityToRedis(UserSessionHistory userSessionHistory) {
        String redisKey = RedisUtils.formatUserSessionActivityKey(userSessionHistory.getUserId());
        redisTemplate
                .opsForValue()
                .set(redisKey, userSessionHistory);
    }


    @Override
    public void close() {
        Processor.super.close();
    }
}
