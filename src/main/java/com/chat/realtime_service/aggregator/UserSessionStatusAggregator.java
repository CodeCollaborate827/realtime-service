package com.chat.realtime_service.aggregator;

import com.chat.realtime_service.events.upstream.Session;
import com.chat.realtime_service.models.ActiveSession;
import com.chat.realtime_service.models.OldSession;
import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Aggregator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.chat.realtime_service.constants.ApplicationConstants.SESSION_END;
import static com.chat.realtime_service.constants.ApplicationConstants.SESSION_START;

@Slf4j
public class UserSessionStatusAggregator implements Aggregator<String, Session, UserSessionActivity> {
    private static final int MAX_OLD_SESSIONS_LIMIT = 5;
    private RedisTemplate<String, UserSessionActivity> redisTemplate;

    public UserSessionStatusAggregator(RedisTemplate<String, UserSessionActivity> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    public UserSessionActivity apply(String userId, Session sessionStatusEvent, UserSessionActivity userSessionActivity) {
        log.info("Processing for user: {} session event: {}", userId, sessionStatusEvent);
        if (userSessionActivity.getUserId() == null) {
            userSessionActivity.setUserId(userId);
        }

        if (!userSessionActivity.getUserId().equals(sessionStatusEvent.getUserId())) {
            log.error("Cannot update session activity for user id: {} with session of user id: {}", userSessionActivity.getUserId(), sessionStatusEvent.getUserId());
        }

        if (userSessionActivity.getActiveSessions() == null) {
            log.debug("Active sessions is null, creating new list");
            userSessionActivity.setActiveSessions(new ArrayList<>());
        }

        if (userSessionActivity.getPreviousSessions() == null) {
            log.debug("Previous sessions is null, creating new list");
            userSessionActivity.setPreviousSessions(new ArrayList<>());
        }

        String sessionStatus = sessionStatusEvent.getStatus();
        String sessionId = sessionStatusEvent.getSessionId();
        if (SESSION_START.equals(sessionStatus)) {
            log.info("Adding new active session id: {} for user: {}", sessionId, userId);
            addCurrentSession(userSessionActivity, sessionStatusEvent);
        } else if (SESSION_END.equals(sessionStatus)) {
            log.info("Removing active session id: {} for user: {}", sessionId, userId);
            removeCurrentSessionAndAddPreviousSession(userSessionActivity, sessionStatusEvent);
        } else {
            log.error("Invalid status of session: {} for session id: {}", sessionStatus, sessionStatusEvent.getSessionId());
        }

        return userSessionActivity;
    }


    private void addCurrentSession(UserSessionActivity userSessionActivity, Session sessionStatusEvent) {

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

        // store the updated user session activity for querying
        storeUserSessionActivity(userSessionActivity);
    }

    private void removeCurrentSessionAndAddPreviousSession(UserSessionActivity userSessionActivity, Session sessionStatusEvent) {
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

        // store the updated user session activity for querying
        storeUserSessionActivity(userSessionActivity);
    }

    private static void cleanUpOldSession(UserSessionActivity userSessionActivity) {
        List<OldSession> previousSessions = userSessionActivity.getPreviousSessions();
        while (previousSessions.size() > MAX_OLD_SESSIONS_LIMIT) {
            userSessionActivity.getPreviousSessions().removeLast();
        }
    }

    private void storeUserSessionActivity(UserSessionActivity userSessionActivity) {
        String redisKey = RedisUtils.formatUserSessionActivityKey(userSessionActivity.getUserId());
        redisTemplate
                .opsForValue()
                .set(redisKey, userSessionActivity);
    }




}
