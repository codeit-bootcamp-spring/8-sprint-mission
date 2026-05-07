package com.sprint.mission.discodeit.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisLockProvider {

  private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
  private static final String LOCK_KEY_PREFIX = "lock:";
  private static final String UNLOCK_SCRIPT =
      "if redis.call('get', KEYS[1]) == ARGV[1] then " +
          "  return redis.call('del', KEYS[1]) " +
          "else return 0 end";

  private final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();
  private final RedisTemplate<String, Object> redisTemplate;

  public void acquireLock(String key) {
    String lockKey = LOCK_KEY_PREFIX + key;
    // 고유 주인 값 생성
    String myValue = UUID.randomUUID().toString();

    ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

    // SETNX: 키가 없으면 설정하고 TTL 지정
    Boolean acquired = valueOps.setIfAbsent(lockKey, myValue, LOCK_TIMEOUT);

    if (Boolean.TRUE.equals(acquired)) {
      lockValueHolder.set(myValue);
      log.debug("분산 락 획득 성공: {} (값: {})", lockKey, myValue);
    } else {
      log.debug("분산 락 획득 실패: {}", lockKey);
      throw new RedisLockAcquisitionException("분산 락 획득 실패: " + lockKey);
    }
  }

  public void releaseLock(String key) {
    String lockKey = LOCK_KEY_PREFIX + key;
    String myValue = lockValueHolder.get();

    if (myValue == null) {
      return;
    }

    try {
      redisTemplate.execute(
          new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
          Collections.singletonList(lockKey),
          myValue
      );
      log.debug("분산 락 해제 완료: {}", lockKey);
    } catch (Exception e) {
      log.warn("분산 락 해제 실패: {}", lockKey, e);
    } finally {
      lockValueHolder.remove();
    }
  }

  public static class RedisLockAcquisitionException extends RuntimeException {

    public RedisLockAcquisitionException(String message) {
      super(message);
    }
  }
}
