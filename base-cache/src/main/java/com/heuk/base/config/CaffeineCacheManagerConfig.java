package com.heuk.base.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "caffeine")
public class CaffeineCacheManagerConfig {

    @Bean("baseCache")
    public CacheManager baseCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.SECONDS)
                        .initialCapacity(100)
                        .maximumSize(1000)
        );
        return cacheManager;
    }

    @Bean
    public CacheManager cacheManager() {
        return this.baseCacheManager();
    }
}
