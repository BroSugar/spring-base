package com.heuk.base.controller;

import com.heuk.base.service.RedissonPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/redisson/pubsub")
public class RedissonPubSubController {

    private final RedissonPubSubService pubSubService;
    private final ReactiveRedisMessageListenerContainer redisContainer;

    public RedissonPubSubController(RedissonPubSubService pubSubService,
                                    ReactiveRedisMessageListenerContainer redisContainer) {
        this.pubSubService = pubSubService;
        this.redisContainer = redisContainer;
    }

    @GetMapping(value = "/listener", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> listenerChannel() {
        log.info("listener redis channel............");
        String channelPrefix = "TEST";
        return redisContainer
                .receive(PatternTopic.of(channelPrefix))
                .map(message -> {
                    log.info("channel >>> {}, message >>> {}", message.getChannel(), message.getMessage());
                    return message.getMessage();
                });
    }

    // 添加监听器
    @GetMapping("/subscribe")
    public String subscribe(@RequestParam String channel) {
        pubSubService.subscribe(channel);
        return "已订阅频道: " + channel;
    }

    // 移除监听器
    @GetMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String channel) {
        pubSubService.unsubscribe(channel);
        return "已取消订阅频道: " + channel;
    }

    // 添加通配符监听器
    @GetMapping("/subscribe-pattern")
    public String subscribePattern(@RequestParam String pattern) {
        pubSubService.subscribePattern(pattern);
        return "已添加通配符监听器: " + pattern;
    }

    // 发送消息
    @GetMapping("/publish")
    public String publish(@RequestParam String channel, @RequestParam String msg) {
        pubSubService.publish(channel, msg);
        return "消息已发送到频道: " + channel;
    }
}
