package com.carddemo.account.messaging;

import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountMqListener {

    private static final Logger log = LoggerFactory.getLogger(AccountMqListener.class);
    public static final String ACCOUNT_REQUEST_QUEUE = "carddemo.account.request";
    public static final String ACCOUNT_REPLY_QUEUE = "carddemo.account.reply";

    private final AccountService accountService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public AccountMqListener(AccountService accountService,
                             RabbitTemplate rabbitTemplate,
                             ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = ACCOUNT_REQUEST_QUEUE)
    public void handleAccountInquiry(String message) {
        log.info("Received account inquiry request: {}", message);
        try {
            Long acctId = Long.parseLong(message.trim());
            AccountDto account = accountService.getAccount(acctId);
            String response = objectMapper.writeValueAsString(account);
            rabbitTemplate.convertAndSend(ACCOUNT_REPLY_QUEUE, response);
            log.info("Account inquiry response sent for account {}", acctId);
        } catch (Exception e) {
            log.error("Error processing account inquiry: {}", e.getMessage(), e);
            rabbitTemplate.convertAndSend(ACCOUNT_REPLY_QUEUE,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
