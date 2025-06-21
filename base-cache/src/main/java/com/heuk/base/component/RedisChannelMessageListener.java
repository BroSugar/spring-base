package com.heuk.base.component;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisChannelMessageListener implements MessageListener<String> {
    @Override
    public void onMessage(CharSequence charSequence, String s) {
        log.info("redis channel message listener >>> {}", s);
    }
}
