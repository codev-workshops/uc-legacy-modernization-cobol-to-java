package com.carddemo.account.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DateMqListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private DateMqListener listener;

    @BeforeEach
    void setUp() {
        listener = new DateMqListener(rabbitTemplate);
    }

    @Test
    void handleDateRequest_returnsCurrentDate() {
        listener.handleDateRequest("get-date");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq(DateMqListener.DATE_REPLY_QUEUE),
                responseCaptor.capture());

        String expectedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertEquals(expectedDate, responseCaptor.getValue());
    }

    @Test
    void handleDateRequest_anyMessage_returnsDate() {
        listener.handleDateRequest("");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq(DateMqListener.DATE_REPLY_QUEUE),
                responseCaptor.capture());
        assertEquals(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), responseCaptor.getValue());
    }
}
