package com.heuk.base.config;

import com.heuk.base.utils.SerializerUtil;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(value = "spring.cache.type",havingValue = "redis")
public class RedisCacheManagerConfig {

    @Bean("baseCache")
    public RedisCacheConfiguration baseCacheManagerConfig(CacheProperties cacheProperties) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration.serializeValuesWith(RedisSerializationContext
                .SerializationPair
                .fromSerializer(SerializerUtil.getJacksonSerializer()));
        CacheProperties.Redis redisCacheProperties = cacheProperties.getRedis();
        if (null != redisCacheProperties.getTimeToLive()) {
            configuration = configuration.entryTtl(redisCacheProperties.getTimeToLive());
        }
        if (!redisCacheProperties.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redisCacheProperties.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        if (redisCacheProperties.isUseKeyPrefix() && StringUtils.hasText(redisCacheProperties.getKeyPrefix())) {
            configuration = configuration.prefixCacheNameWith("");
        }
        return configuration;
    }

    @Bean
    public RedisCacheConfiguration cacheManager(CacheProperties cacheProperties) {
        CacheProperties.Redis redisCacheProperties = cacheProperties.getRedis();
        RedisCacheConfiguration cacheConfiguration = this.baseCacheManagerConfig(cacheProperties);
        if (redisCacheProperties.isUseKeyPrefix() && StringUtils.hasText(redisCacheProperties.getKeyPrefix())) {
            cacheConfiguration = cacheConfiguration.prefixCacheNameWith(redisCacheProperties.getKeyPrefix());
        }
        return cacheConfiguration;
    }
}
