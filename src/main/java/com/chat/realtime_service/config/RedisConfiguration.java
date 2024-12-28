package com.chat.realtime_service.config;

import com.chat.realtime_service.models.UserSessionHistory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, UserSessionHistory> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserSessionHistory> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<UserSessionHistory> serializer = new Jackson2JsonRedisSerializer<>(UserSessionHistory.class);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }
}
