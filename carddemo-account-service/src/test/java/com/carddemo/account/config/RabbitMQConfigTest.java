package com.carddemo.account.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void accountRequestQueue() {
        Queue queue = config.accountRequestQueue();
        assertNotNull(queue);
        assertEquals("carddemo.account.request", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void accountReplyQueue() {
        Queue queue = config.accountReplyQueue();
        assertNotNull(queue);
        assertEquals("carddemo.account.reply", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void dateRequestQueue() {
        Queue queue = config.dateRequestQueue();
        assertNotNull(queue);
        assertEquals("carddemo.date.request", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void dateReplyQueue() {
        Queue queue = config.dateReplyQueue();
        assertNotNull(queue);
        assertEquals("carddemo.date.reply", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void messageConverter() {
        assertNotNull(config.messageConverter());
    }
}
