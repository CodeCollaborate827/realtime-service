package com.chat.realtime_service.config;

import com.chat.realtime_service.models.UserSessionActivity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, UserSessionActivity> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserSessionActivity> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<UserSessionActivity> serializer = new Jackson2JsonRedisSerializer<>(UserSessionActivity.class);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }
}
