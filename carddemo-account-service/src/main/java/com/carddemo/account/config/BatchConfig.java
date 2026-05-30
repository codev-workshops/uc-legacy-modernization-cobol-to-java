package com.carddemo.account.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {
    // Spring Batch auto-configuration is handled via application.yml
    // spring.batch.job.enabled=false prevents jobs from running at startup
}
