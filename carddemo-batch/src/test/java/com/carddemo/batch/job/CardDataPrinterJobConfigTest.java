package com.carddemo.batch.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CardDataPrinterJobConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void jobBeanIsCreated() {
        Job job = context.getBean("cardDataPrinterJob", Job.class);
        assertNotNull(job);
    }

    @Test
    void stepBeanIsCreated() {
        Step step = context.getBean("cardDataPrinterStep", Step.class);
        assertNotNull(step);
    }

    @Test
    void readerBeanIsCreated() {
        JpaCursorItemReader<?> reader = context.getBean("cardItemReader", JpaCursorItemReader.class);
        assertNotNull(reader);
    }

    @Test
    void writerBeanIsCreated() {
        FlatFileItemWriter<?> writer = context.getBean("cardReportWriter", FlatFileItemWriter.class);
        assertNotNull(writer);
    }

    @Test
    void lineAggregatorBeanIsCreated() {
        CardRecordLineAggregator aggregator = context.getBean(CardRecordLineAggregator.class);
        assertNotNull(aggregator);
    }
}
