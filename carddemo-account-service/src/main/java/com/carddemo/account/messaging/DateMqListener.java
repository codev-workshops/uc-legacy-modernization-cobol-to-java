package com.carddemo.account.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DateMqListener {

    private static final Logger log = LoggerFactory.getLogger(DateMqListener.class);
    public static final String DATE_REQUEST_QUEUE = "carddemo.date.request";
    public static final String DATE_REPLY_QUEUE = "carddemo.date.reply";

    private final RabbitTemplate rabbitTemplate;

    public DateMqListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = DATE_REQUEST_QUEUE)
    public void handleDateRequest(String message) {
        log.info("Received date request: {}", message);
        String businessDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        rabbitTemplate.convertAndSend(DATE_REPLY_QUEUE, businessDate);
        log.info("Business date response sent: {}", businessDate);
    }
}
