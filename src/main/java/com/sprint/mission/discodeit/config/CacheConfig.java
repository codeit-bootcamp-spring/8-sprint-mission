package com.sprint.mission.discodeit.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();

    cacheManager.setCaches(List.of(
            new CaffeineCache("channel",
                Caffeine.newBuilder()
                    .maximumSize(500)
                    .recordStats()
                    .build()),
            new CaffeineCache("notification",
                Caffeine.newBuilder()
                    .expireAfterAccess(1, TimeUnit.HOURS)
                    .maximumSize(1000)
                    .recordStats()
                    .build()),
            new CaffeineCache("user",
                Caffeine.newBuilder()
                    .expireAfterAccess(1, TimeUnit.HOURS)
                    .maximumSize(1000)
                    .recordStats()
                    .build())
        )
    );

    return cacheManager;
  }
}
