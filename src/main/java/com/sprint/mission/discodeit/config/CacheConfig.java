package com.sprint.mission.discodeit.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration(
      @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer) {

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
        )
        .prefixCacheNameWith("discodeit:")
        .entryTtl(Duration.ofSeconds(600))
        .disableCachingNullValues();
  }
}