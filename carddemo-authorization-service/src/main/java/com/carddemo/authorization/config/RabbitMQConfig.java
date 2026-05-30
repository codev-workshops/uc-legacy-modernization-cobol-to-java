package com.carddemo.authorization.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {

    public static final String AUTH_REQUEST_QUEUE = "carddemo.auth.request";
    public static final String AUTH_REPLY_QUEUE = "carddemo.auth.reply";

    @Bean
    Queue authRequestQueue() {
        return new Queue(AUTH_REQUEST_QUEUE, true);
    }

    @Bean
    Queue authReplyQueue() {
        return new Queue(AUTH_REPLY_QUEUE, true);
    }

    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("*"));
        return converter;
    }
}
