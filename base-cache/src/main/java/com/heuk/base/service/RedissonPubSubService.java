package com.heuk.base.service;

import com.heuk.base.component.RedisChannelMessageListener;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedissonPubSubService {
    private final RedissonClient redissonClient;
    private final RedisChannelMessageListener messageListener;

    private final Map<String, RTopic> topicMap = new HashMap<>();
    private final Map<String, RPatternTopic> patternTopicMap = new HashMap<>();

    public RedissonPubSubService(RedissonClient redissonClient, RedisChannelMessageListener messageListener) {
        this.redissonClient = redissonClient;
        this.messageListener = messageListener;
    }

    @PostConstruct
    public void init() {
        // 可选：初始化默认监听器
    }

    // 订阅指定频道
    public void subscribe(String channel) {
        if (topicMap.containsKey(channel)) {
            System.out.println("频道已存在: " + channel);
            return;
        }

        RTopic topic = redissonClient.getTopic(channel);
        int listenerId = topic.addListener(String.class, messageListener);
        topicMap.put(channel, topic);

        System.out.println("已订阅频道: " + channel + ", listener ID: " + listenerId);
    }

    // 取消订阅指定频道
    public void unsubscribe(String channel) {
        RTopic topic = topicMap.get(channel);
        if (topic != null) {
            topic.removeListener(messageListener);
            topicMap.remove(channel);
            System.out.println("已取消订阅频道: " + channel);
        } else {
            System.out.println("未找到频道: " + channel);
        }
    }

    // 使用通配符订阅频道（如 my-*）
    public void subscribePattern(String pattern) {
        if (patternTopicMap.containsKey(pattern)) {
            System.out.println("通配符频道已存在: " + pattern);
            return;
        }

        RPatternTopic patternTopic = redissonClient.getPatternTopic(pattern);
        int listenerId = patternTopic.addListener(String.class, (pattern1, channel, msg) -> {
            System.out.println("通配符匹配 [" + pattern1 + "]，频道 [" + channel + "]，消息 [" + msg + "]");
        });

        patternTopicMap.put(pattern, patternTopic);
        System.out.println("已添加通配符监听器: " + pattern + "，listener ID: " + listenerId);
    }

    // 发布消息到指定频道
    public void publish(String channel, String message) {
        RTopic topic = redissonClient.getTopic(channel);
        topic.publish(message);
    }
}
