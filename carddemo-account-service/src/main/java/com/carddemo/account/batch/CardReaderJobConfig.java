package com.carddemo.account.batch;

import com.carddemo.account.entity.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static com.carddemo.account.batch.AccountReaderJobConfig.formatLong;
import static com.carddemo.account.batch.AccountReaderJobConfig.padRight;

@Configuration
public class CardReaderJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CardReaderJobConfig.class);

    @Bean
    public JdbcCursorItemReader<Card> cardItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Card>()
                .name("cardItemReader")
                .dataSource(dataSource)
                .sql("SELECT card_num, card_acct_id, card_cvv_cd, card_embossed_name, " +
                        "card_expiration_date, card_active_status FROM cards ORDER BY card_num")
                .rowMapper((rs, rowNum) -> Card.builder()
                        .cardNum(rs.getString("card_num"))
                        .cardAcctId(rs.getLong("card_acct_id"))
                        .cardCvvCd(rs.getObject("card_cvv_cd", Integer.class))
                        .cardEmbossedName(rs.getString("card_embossed_name"))
                        .cardExpirationDate(rs.getString("card_expiration_date"))
                        .cardActiveStatus(rs.getString("card_active_status"))
                        .build())
                .build();
    }

    @Bean
    public FlatFileItemWriter<Card> cardItemWriter() {
        return new FlatFileItemWriterBuilder<Card>()
                .name("cardItemWriter")
                .resource(new FileSystemResource("output/cards.dat"))
                .lineAggregator(CardReaderJobConfig::formatCard)
                .build();
    }

    @Bean
    public Job cardReaderJob(JobRepository jobRepository, Step cardReaderStep) {
        return new JobBuilder("cardReaderJob", jobRepository)
                .start(cardReaderStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Card reader job completed with status: {}. Records written: {}",
                                jobExecution.getStatus(),
                                jobExecution.getStepExecutions().stream()
                                        .mapToLong(se -> se.getWriteCount())
                                        .sum());
                    }
                })
                .build();
    }

    @Bean
    public Step cardReaderStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               JdbcCursorItemReader<Card> cardItemReader,
                               FlatFileItemWriter<Card> cardItemWriter) {
        return new StepBuilder("cardReaderStep", jobRepository)
                .<Card, Card>chunk(100, transactionManager)
                .reader(cardItemReader)
                .writer(cardItemWriter)
                .build();
    }

    static String formatCard(Card card) {
        StringBuilder sb = new StringBuilder(150);
        // CARD-NUM PIC X(16)
        sb.append(padRight(card.getCardNum(), 16));
        // CARD-ACCT-ID PIC 9(11)
        sb.append(formatLong(card.getCardAcctId(), 11));
        // CARD-CVV-CD PIC 9(03)
        sb.append(String.format("%03d", card.getCardCvvCd() != null ? card.getCardCvvCd() : 0));
        // CARD-EMBOSSED-NAME PIC X(50)
        sb.append(padRight(card.getCardEmbossedName(), 50));
        // CARD-EXPIRAION-DATE PIC X(10)
        sb.append(padRight(card.getCardExpirationDate(), 10));
        // CARD-ACTIVE-STATUS PIC X(01)
        sb.append(padRight(card.getCardActiveStatus(), 1));
        // FILLER PIC X(59) to reach 150 bytes
        int remaining = 150 - sb.length();
        if (remaining > 0) {
            sb.append(String.format("%-" + remaining + "s", ""));
        }
        return sb.substring(0, 150);
    }
}
