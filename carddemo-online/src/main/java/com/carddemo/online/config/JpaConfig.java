package com.carddemo.online.config;

import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.service.TransactionValidationService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.carddemo.common.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.common.repository")
public class JpaConfig {

    @Bean
    public TransactionValidationService transactionValidationService(
            CardXrefRepository cardXrefRepository,
            AccountRepository accountRepository) {
        return new TransactionValidationService(cardXrefRepository, accountRepository);
    }
}
