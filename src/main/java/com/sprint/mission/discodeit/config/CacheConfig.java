package com.sprint.mission.discodeit.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    private static void onRemoval(Object key, Object value, RemovalCause cause) {
        switch (cause) {
            case SIZE:
                log.debug("캐시 크기 초과로 인한 엔트리 제거 - key: {}", key);
                break;
            case EXPIRED:
                log.debug("만료 시간 도달로 인한 엔트리 제거 - key: {}", key);
                break;
            case EXPLICIT:
                log.debug("수동 삭제로 인한 엔트리 제거 - key: {}", key);
                break;
            case REPLACED:
                log.debug("새 값으로 교체로 인한 엔트리 제거 - key: {}", key);
                break;
            default:
                log.debug("캐시 엔트리 제거: key: {}, cause: {}", key, cause);
        }
    }

    @Bean
    @Primary
    public CacheManager compositeCacheManager() {

        log.info("복합 캐시 매니저 초기화");

        // 여러 CacheManager를 조합하기 위해서는 CompositeCacheManager를 사용
        CompositeCacheManager composite = new CompositeCacheManager();

        // setCacheManagers() 메서드를 통해 여러 CacheManager를 조합할 수 있음
        composite.setCacheManagers(List.of(
                channelCacheManager(),
                notificationCacheManager(),
                userCacheManager()
        ));

        // 캐시가 없으면 에러 발생 (NoOpCache 비활성화)
        composite.setFallbackToNoOpCache(false);

        log.info("복합 캐시 매니저 설정 완료");

        return composite;
    }

    // 사용자별 채널 목록 조회 전용
    @Bean
    public CacheManager channelCacheManager() {

        log.info("channelCacheManager 초기화 - channel 도메인용");

        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Caffeine 고급 캐시 정책 설정
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(250)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .removalListener(CacheConfig::onRemoval)
        );

        manager.setCacheNames(List.of("channels"));

        log.info("channelCacheManager 설정 완료 - 캐시: [channel]");

        return manager;
    }

    @Bean
    public CacheManager notificationCacheManager() {

        log.info("notificationCacheManager 초기화 - notification 도메인용");

        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .removalListener(CacheConfig::onRemoval)
        );

        manager.setCacheNames(List.of("notifications"));

        log.info("notificationCacheManager 설정 완료 - 캐시: [notification]");

        return manager;
    }

    @Bean
    public CacheManager userCacheManager() {

        log.info("userCacheManager 초기화 - user 도메인용");

        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .removalListener(CacheConfig::onRemoval)
        );

        manager.setCacheNames(List.of("users"));

        log.info("userCacheManager 설정 완료 - 캐시: [users]");

        return manager;
    }
}
