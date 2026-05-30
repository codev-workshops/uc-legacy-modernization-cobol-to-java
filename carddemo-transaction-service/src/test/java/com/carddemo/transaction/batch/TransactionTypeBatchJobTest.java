package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.file.FlatFileItemReader;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionTypeBatchJobTest {

    @Mock
    private TransactionTypeRepository transactionTypeRepository;

    @InjectMocks
    private TransactionTypeBatchJob transactionTypeBatchJob;

    @Test
    void transactionTypeReader_isCreated() {
        FlatFileItemReader<TransactionType> reader = transactionTypeBatchJob.transactionTypeReader();
        assertNotNull(reader);
    }

    @Test
    void transactionTypeProcessor_setsCreatedAt() throws Exception {
        var processor = transactionTypeBatchJob.transactionTypeProcessor();
        TransactionType type = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        TransactionType result = processor.process(type);
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void transactionTypeProcessor_preservesExistingCreatedAt() throws Exception {
        var processor = transactionTypeBatchJob.transactionTypeProcessor();
        java.time.LocalDateTime existing = java.time.LocalDateTime.of(2024, 1, 1, 0, 0);
        TransactionType type = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .createdAt(existing)
                .build();

        TransactionType result = processor.process(type);
        assertNotNull(result);
        assertEquals(existing, result.getCreatedAt());
    }

    @Test
    void transactionTypeWriter_isCreated() {
        var writer = transactionTypeBatchJob.transactionTypeWriter();
        assertNotNull(writer);
    }
}
