package com.chat.realtime_service.controller;

import com.chat.realtime_service.models.UserSessionActivity;
import com.chat.realtime_service.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequestMapping("/session")
@Slf4j
@RequiredArgsConstructor
public class UserSessionController {
    private final RedisTemplate<String, UserSessionActivity> redisTemplate;

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<Object>> queryUserSessionActivity(@PathVariable String userId) {
        // TODO: Implement the logic in service layer
        // TODO: handle the case where the user session activity is not found
        String redisKey = RedisUtils.formatUserSessionActivityKey(userId);
        return Mono.defer(() -> Mono.just(Objects.requireNonNull(redisTemplate.opsForValue().get(redisKey))))
                .map(ResponseEntity::ok);
    }
}
