package com.carddemo.authorization.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void authRequestQueue_createsQueue() {
        Queue queue = config.authRequestQueue();
        assertNotNull(queue);
        assertEquals("carddemo.auth.request", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void authReplyQueue_createsQueue() {
        Queue queue = config.authReplyQueue();
        assertNotNull(queue);
        assertEquals("carddemo.auth.reply", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void messageConverter_createsConverter() {
        assertNotNull(config.messageConverter());
    }
}
