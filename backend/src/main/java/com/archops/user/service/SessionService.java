package com.archops.user.service;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private static final String SESSION_KEY_PREFIX = "session:user:";

    private final StringRedisTemplate redisTemplate;
    private final Duration sessionTtl;

    public SessionService(StringRedisTemplate redisTemplate, com.archops.common.config.JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.sessionTtl = Duration.ofMillis(jwtProperties.refreshTokenExpirationMs());
    }

    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        redisTemplate
                .opsForValue()
                .set(sessionKey(userId), sessionId, sessionTtl);
        return sessionId;
    }

    public boolean isSessionValid(Long userId, String sessionId) {
        String currentSessionId = redisTemplate.opsForValue().get(sessionKey(userId));
        return sessionId != null && sessionId.equals(currentSessionId);
    }

    public void invalidateSession(Long userId) {
        redisTemplate.delete(sessionKey(userId));
    }

    private String sessionKey(Long userId) {
        return SESSION_KEY_PREFIX + userId;
    }
}
