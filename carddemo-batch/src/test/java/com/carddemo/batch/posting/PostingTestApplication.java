package com.carddemo.batch.posting;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal Spring Boot context for posting-related tests.
 * Scans only the posting package to avoid the pre-existing
 * transactionReader bean name conflict between
 * TransactionBackupJobConfig and TransactionReportJobConfig.
 */
@SpringBootApplication(scanBasePackages = "com.carddemo.batch.posting")
@EntityScan(basePackages = "com.carddemo.common.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.common.repository")
class PostingTestApplication {
}
