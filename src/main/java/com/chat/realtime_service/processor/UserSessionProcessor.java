package com.chat.realtime_service.processor;

import com.chat.realtime_service.events.upstream.UserSessionEvent;
import com.chat.realtime_service.models.ActiveSession;
import com.chat.realtime_service.models.OldSession;
import com.chat.realtime_service.models.UserSessionActivity;
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
public class UserSessionProcessor implements Processor<String, UserSessionEvent, String, UserSessionActivity> {
    private static final int MAX_OLD_SESSIONS_LIMIT = 5;

    private String storeName;
    private ProcessorContext<String, UserSessionActivity> context;
    private KeyValueStore<String, UserSessionActivity> store;
    private RedisTemplate<String, UserSessionActivity> redisTemplate;


    public UserSessionProcessor(String storeName, RedisTemplate<String, UserSessionActivity> redisTemplate) {
        this.storeName = storeName;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void init(ProcessorContext<String, UserSessionActivity> context) {
        this.context = context;
        this.store = context.getStateStore(storeName);
    }

    @Override
    public void process(Record<String, UserSessionEvent> record) {

        String key = record.key();
        UserSessionEvent userSessionEvent = record.value();

        String userId = userSessionEvent.getUserId();

        UserSessionActivity userSessionActivity = store.get(userId);
        // init if not exists
        if (userSessionActivity == null) {
            log.info("Init user session activity for user id: {}", userId);
            userSessionActivity = UserSessionActivity.builder()
                    .activeSessions(new ArrayList<>())
                    .previousSessions(new ArrayList<>())
                    .userId(userId)
                    .build();
        }

        String sessionStatus = userSessionEvent.getStatus();
        String sessionId = userSessionEvent.getSessionId();

        if (!SESSION_START.equals(sessionStatus) && !SESSION_END.equals(sessionStatus)) {
            log.error("Invalid status of session: {} for session id: {}, userId: {}", sessionStatus, sessionId, userId);
            return; // drop the message
        }

        // process the message based on the session status
        if (SESSION_START.equals(sessionStatus)) {
            log.info("Adding new active session id: {} for user: {}", sessionId, userId);
            addCurrentSession(userSessionActivity, userSessionEvent);
        } else if (SESSION_END.equals(sessionStatus)) {
            log.info("Removing active session id: {} for user: {}", sessionId, userId);
            removeCurrentSessionAndAddPreviousSession(userSessionActivity, userSessionEvent);
        }

        // store the updated user session activity to the local store
        store.put(userId, userSessionActivity);
        // store the updated user session activity for querying
        storeUserSessionActivityToRedis(userSessionActivity);

        // forward the new record to downstream
        Record<String, UserSessionActivity> newRecord = new Record<>(userId, userSessionActivity, record.timestamp(), record.headers());
        context.forward(newRecord);
    }

    private void addCurrentSession(UserSessionActivity userSessionActivity, UserSessionEvent sessionStatusEvent) {

        boolean alreadyExists = userSessionActivity.getActiveSessions().stream().anyMatch(session -> session.getSessionId().equals(sessionStatusEvent.getSessionId()));

        if (alreadyExists) {
            log.warn("User session id: {} already exists as current active session for user id: {}", sessionStatusEvent.getSessionId(), userSessionActivity.getUserId());
            return;
        }

        ActiveSession userCurrentSession = ActiveSession.builder()
                .sessionId(sessionStatusEvent.getSessionId())
                .userId(sessionStatusEvent.getUserId())
                .startTime(sessionStatusEvent.getTimestamp())
                .clientIp(sessionStatusEvent.getClientIp())
                .build();

        userSessionActivity.getActiveSessions().add(userCurrentSession);

    }

    private void removeCurrentSessionAndAddPreviousSession(UserSessionActivity userSessionActivity, UserSessionEvent sessionStatusEvent) {
        String sessionId = sessionStatusEvent.getSessionId();

        // get the session with that id, remove from currentSession list and add it to previousSession list
        Optional<ActiveSession> session = userSessionActivity.getActiveSessions()
                .stream()
                .filter(currentSession -> currentSession.getSessionId().equals(sessionId))
                .findFirst();

        if (session.isEmpty()) {
            log.error("User session id: {} does not existing as current active session for user id: {}", sessionId, userSessionActivity.getUserId());
            return;
        }
        userSessionActivity.getActiveSessions().removeIf(currentSession -> currentSession.getSessionId().equals(sessionId));

        log.info("SESSION: {}", session.get());
        OldSession oldSession = OldSession.builder()
                .sessionId(sessionStatusEvent.getSessionId())
                .userId(sessionStatusEvent.getUserId())
                .startTime(session.get().getStartTime())
                .clientIp(sessionStatusEvent.getClientIp())
                .endTime(sessionStatusEvent.getTimestamp())
                .build();

        userSessionActivity.getPreviousSessions().addFirst(oldSession); // sort by latest
        cleanUpOldSession(userSessionActivity);


    }

    private static void cleanUpOldSession(UserSessionActivity userSessionActivity) {
        List<OldSession> previousSessions = userSessionActivity.getPreviousSessions();
        while (previousSessions.size() > MAX_OLD_SESSIONS_LIMIT) {
            userSessionActivity.getPreviousSessions().removeLast();
        }
    }

    private void storeUserSessionActivityToRedis(UserSessionActivity userSessionActivity) {
        String redisKey = RedisUtils.formatUserSessionActivityKey(userSessionActivity.getUserId());
        redisTemplate
                .opsForValue()
                .set(redisKey, userSessionActivity);
    }


    @Override
    public void close() {
        Processor.super.close();
    }
}
