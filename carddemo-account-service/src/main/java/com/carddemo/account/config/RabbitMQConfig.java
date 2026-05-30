package com.carddemo.account.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNT_REQUEST_QUEUE = "carddemo.account.request";
    public static final String ACCOUNT_REPLY_QUEUE = "carddemo.account.reply";
    public static final String DATE_REQUEST_QUEUE = "carddemo.date.request";
    public static final String DATE_REPLY_QUEUE = "carddemo.date.reply";

    @Bean
    Queue accountRequestQueue() {
        return new Queue(ACCOUNT_REQUEST_QUEUE, true);
    }

    @Bean
    Queue accountReplyQueue() {
        return new Queue(ACCOUNT_REPLY_QUEUE, true);
    }

    @Bean
    Queue dateRequestQueue() {
        return new Queue(DATE_REQUEST_QUEUE, true);
    }

    @Bean
    Queue dateReplyQueue() {
        return new Queue(DATE_REPLY_QUEUE, true);
    }

    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("*"));
        return converter;
    }
}
