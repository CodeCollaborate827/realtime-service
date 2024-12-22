package com.chat.realtime_service.utils;

public class RedisUtils {

    private static final String USER_SESSION_ACTIVITY_KEY_FORMAT = "USER_SESSION_ACTIVITY:{%s}";

    public static String formatUserSessionActivityKey(String userId) {
        return USER_SESSION_ACTIVITY_KEY_FORMAT.formatted(userId);
    }
}
