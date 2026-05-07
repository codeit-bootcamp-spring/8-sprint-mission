package com.sprint.mission.discodeit.global.redis;

import com.sprint.mission.discodeit.global.exception.DiscodeitException;
import com.sprint.mission.discodeit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisLockProvider
{
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final RedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final RedisTemplate<String, Object> redisTemplate;

    public String acquireLock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TIMEOUT);

        if (!Boolean.TRUE.equals(acquired)) {
            throw new RedisLockAcquisitionException("분산 락 획득 실패: " + lockKey);
        }

        return lockValue;
    }

    public void releaseLock(String key, String lockValue) {
        String lockKey = LOCK_KEY_PREFIX + key;
        redisTemplate.execute(RELEASE_SCRIPT, List.of(lockKey), lockValue);
    }
    
    public static class RedisLockAcquisitionException extends DiscodeitException {
        public RedisLockAcquisitionException(String lockKey) {
            super(ErrorCode.LOCK_ACQUISITION_FAILED, Map.of("lockKey", lockKey));
        }
    }
}
