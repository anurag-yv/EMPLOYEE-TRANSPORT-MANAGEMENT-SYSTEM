package com.example.employee_transport_system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Cache configuration utilizing Redis with error fallback handling.
 */
@Configuration
public class RedisConfig implements CachingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public CacheManager cacheManager(final RedisConnectionFactory connectionFactory) {
        final RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(final RuntimeException exception,
                                            final Cache cache,
                                            final Object key) {
                LOGGER.warn("Redis GET failure for key {} in cache {}: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(final RuntimeException exception,
                                            final Cache cache,
                                            final Object key,
                                            final Object value) {
                LOGGER.warn("Redis PUT failure for key {} in cache {}: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(final RuntimeException exception,
                                              final Cache cache,
                                              final Object key) {
                LOGGER.warn("Redis EVICT failure for key {} in cache {}: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(final RuntimeException exception,
                                              final Cache cache) {
                LOGGER.warn("Redis CLEAR failure for cache {}: {}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
