package com.chat.realtime_service.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private final static ObjectMapper objectMapper = new ObjectMapper();


    public static String toJson(Object object){
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T toObject(String jsonString, Class<T> t){
        try {
            return objectMapper.readValue(jsonString, t);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T toObject(byte[] bytes, Class<T> t){
        try {
            return toObject(new String(bytes), t);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
