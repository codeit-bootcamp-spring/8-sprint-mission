package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.cache.CacheNames;
import com.sprint.mission.discodeit.event.message.UserLogInOutEvent;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisLockProvider.class)
public class RedisJwtRegistry implements JwtRegistry {

    private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
    private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access_tokens";
    private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh_tokens";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockProvider redisLockProvider;
    private final ObjectMapper objectMapper;

    private String getUserKey(UUID userId) {
        return USER_JWT_KEY_PREFIX + userId;
    }

    private JwtInformation toJwt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof JwtInformation j) {
            return j;
        }
        return objectMapper.convertValue(o, JwtInformation.class);
    }

    private List<JwtInformation> readJwtList(String userKey) {
        List<Object> raw = redisTemplate.opsForList().range(userKey, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<JwtInformation> out = new ArrayList<>(raw.size());
        for (Object o : raw) {
            JwtInformation j = toJwt(o);
            if (j != null) {
                out.add(j);
            }
        }
        return out;
    }

    private void addTokenIndex(String accessToken, String refreshToken) {
        redisTemplate.opsForSet().add(ACCESS_TOKEN_INDEX_KEY, accessToken);
        redisTemplate.opsForSet().add(REFRESH_TOKEN_INDEX_KEY, refreshToken);
        redisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, DEFAULT_TTL);
        redisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, DEFAULT_TTL);
    }

    private void removeTokenIndex(String accessToken, String refreshToken) {
        redisTemplate.opsForSet().remove(ACCESS_TOKEN_INDEX_KEY, accessToken);
        redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);
    }

    @CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
    @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        String userKey = getUserKey(jwtInformation.getUserDto().id());
        String lockKey = jwtInformation.getUserDto().id().toString();
        int maxActiveJwtCount = jwtProperties.maxActiveJwtCount();

        redisLockProvider.acquireLock(lockKey);
        try {
            Long currentSize = redisTemplate.opsForList().size(userKey);
            while (currentSize != null && currentSize >= maxActiveJwtCount) {
                Object oldestTokenObj = redisTemplate.opsForList().leftPop(userKey);
                if (oldestTokenObj instanceof JwtInformation oldestToken) {
                    removeTokenIndex(oldestToken.getAccessToken(), oldestToken.getRefreshToken());
                }
                currentSize = redisTemplate.opsForList().size(userKey);
            }

            redisTemplate.opsForList().rightPush(userKey, jwtInformation);
            redisTemplate.expire(userKey, DEFAULT_TTL);
            addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
        } finally {
            redisLockProvider.releaseLock(lockKey);
        }

        eventPublisher.publishEvent(new UserLogInOutEvent(jwtInformation.getUserDto().id(), true));
    }

    @CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        String userKey = getUserKey(userId);
        String lockKey = userId.toString();
        redisLockProvider.acquireLock(lockKey);
        try {
            List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
            if (tokens != null) {
                tokens.forEach(tokenObj -> {
                    JwtInformation jwtInfo = toJwt(tokenObj);
                    if (jwtInfo != null) {
                        removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                    }
                });
            }
            redisTemplate.delete(userKey);
        } finally {
            redisLockProvider.releaseLock(lockKey);
        }
        eventPublisher.publishEvent(new UserLogInOutEvent(userId, false));
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        String userKey = getUserKey(userId);
        Long size = redisTemplate.opsForList().size(userKey);
        return size != null && size > 0;
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(ACCESS_TOKEN_INDEX_KEY, accessToken.trim())
        );
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return false;
        }
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken)
        );
    }

    @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        String userKey = getUserKey(newJwtInformation.getUserDto().id());
        String lockKey = newJwtInformation.getUserDto().id().toString();

        redisLockProvider.acquireLock(lockKey);
        try {
            List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

            if (tokens != null) {
                for (int i = 0; i < tokens.size(); i++) {
                    JwtInformation jwtInfo = toJwt(tokens.get(i));
                    if (jwtInfo != null && jwtInfo.getRefreshToken().equals(refreshToken)) {
                        removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                        jwtInfo.rotate(
                                newJwtInformation.getAccessToken(),
                                newJwtInformation.getRefreshToken(),
                                newJwtInformation.getAccessTokenExpiry(),
                                newJwtInformation.getRefreshTokenExpiry()
                        );
                        redisTemplate.opsForList().set(userKey, i, jwtInfo);
                        addTokenIndex(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());
                        redisTemplate.expire(userKey, DEFAULT_TTL);
                        break;
                    }
                }
            }
        } finally {
            redisLockProvider.releaseLock(lockKey);
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
        if (userKeys == null || userKeys.isEmpty()) {
            return;
        }

        for (String userKey : userKeys) {
            UUID userId;
            try {
                userId = UUID.fromString(userKey.substring(USER_JWT_KEY_PREFIX.length()));
            } catch (RuntimeException ex) {
                log.warn("JWT 사용자 키 파싱 실패: {}", userKey, ex);
                continue;
            }
            String lockKey = userId.toString();
            try {
                redisLockProvider.acquireLock(lockKey);
            } catch (RedisLockAcquisitionException e) {
                log.debug("만료 정리 스킵(락 점유): {}", userId);
                continue;
            }
            try {
                List<JwtInformation> items = readJwtList(userKey);
                List<JwtInformation> active = new ArrayList<>();
                for (JwtInformation jwtInfo : items) {
                    boolean isExpired =
                            !jwtTokenProvider.validateToken(jwtInfo.getAccessToken())
                                    || !jwtTokenProvider.validateToken(jwtInfo.getRefreshToken());
                    if (isExpired) {
                        removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                    } else {
                        active.add(jwtInfo);
                    }
                }
                if (active.isEmpty()) {
                    redisTemplate.delete(userKey);
                } else if (active.size() != items.size()) {
                    redisTemplate.delete(userKey);
                    for (JwtInformation j : active) {
                        redisTemplate.opsForList().rightPush(userKey, j);
                    }
                    redisTemplate.expire(userKey, DEFAULT_TTL);
                }
            } finally {
                redisLockProvider.releaseLock(lockKey);
            }
        }
    }

    @CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
    @Retryable(retryFor = RedisLockAcquisitionException.class, maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Override
    public void invalidateJwtInformationByRefreshToken(String refreshToken) {
        if (refreshToken == null || !Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken))) {
            return;
        }

        for (String userKey : Optional.ofNullable(redisTemplate.keys(USER_JWT_KEY_PREFIX + "*")).orElse(Set.of())) {
            UUID userId;
            try {
                userId = UUID.fromString(userKey.substring(USER_JWT_KEY_PREFIX.length()));
            } catch (RuntimeException ex) {
                continue;
            }
            String lockKey = userId.toString();
            redisLockProvider.acquireLock(lockKey);
            try {
                List<JwtInformation> items = readJwtList(userKey);
                JwtInformation target = items.stream()
                        .filter(j -> refreshToken.equals(j.getRefreshToken()))
                        .findFirst()
                        .orElse(null);
                if (target == null) {
                    continue;
                }
                removeTokenIndex(target.getAccessToken(), target.getRefreshToken());
                items.removeIf(j -> refreshToken.equals(j.getRefreshToken()));
                redisTemplate.delete(userKey);
                for (JwtInformation j : items) {
                    redisTemplate.opsForList().rightPush(userKey, j);
                }
                if (!items.isEmpty()) {
                    redisTemplate.expire(userKey, DEFAULT_TTL);
                }
                eventPublisher.publishEvent(new UserLogInOutEvent(userId, false));
                return;
            } finally {
                redisLockProvider.releaseLock(lockKey);
            }
        }
        redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);
    }
}
