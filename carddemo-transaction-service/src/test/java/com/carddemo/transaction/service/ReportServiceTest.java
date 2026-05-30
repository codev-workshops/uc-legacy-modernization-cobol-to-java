package com.carddemo.transaction.service;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionCategoryRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionTypeRepository transactionTypeRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateTransactionReport_withData() {
        Transaction tx = Transaction.builder()
                .tranId("TX001")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("ONLINE")
                .tranCardNum("4111111111111111")
                .tranAmt(new BigDecimal("100.00"))
                .build();

        TransactionType type = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        TransactionCategory cat = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        when(transactionRepository.findAll()).thenReturn(List.of(tx));
        when(transactionTypeRepository.findAll()).thenReturn(List.of(type));
        when(transactionCategoryRepository.findAll()).thenReturn(List.of(cat));

        byte[] report = reportService.generateTransactionReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertNotNull(report);
        assertTrue(report.length > 0);
        String content = new String(report);
        assertTrue(content.contains("DALYREPT"));
        assertTrue(content.contains("Daily Transaction Report"));
        assertTrue(content.contains("TX001"));
        assertTrue(content.contains("Grand Total"));
    }

    @Test
    void generateTransactionReport_emptyData() {
        when(transactionRepository.findAll()).thenReturn(List.of());
        when(transactionTypeRepository.findAll()).thenReturn(List.of());
        when(transactionCategoryRepository.findAll()).thenReturn(List.of());

        byte[] report = reportService.generateTransactionReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertNotNull(report);
        String content = new String(report);
        assertTrue(content.contains("DALYREPT"));
        assertTrue(content.contains("Grand Total"));
    }

    @Test
    void generateTransactionReport_verifyFormat() {
        Transaction tx = Transaction.builder()
                .tranId("TX002")
                .tranTypeCd("02")
                .tranCatCd(2)
                .tranSource("ATM")
                .tranCardNum("5222222222222222")
                .tranAmt(new BigDecimal("-50.00"))
                .build();

        TransactionType type = TransactionType.builder()
                .tranType("02")
                .tranTypeDesc("Cash Advance")
                .build();

        when(transactionRepository.findAll()).thenReturn(List.of(tx));
        when(transactionTypeRepository.findAll()).thenReturn(List.of(type));
        when(transactionCategoryRepository.findAll()).thenReturn(List.of());

        byte[] report = reportService.generateTransactionReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        String content = new String(report);
        assertTrue(content.contains("TX002"));
        assertTrue(content.contains("2024-01-01"));
        assertTrue(content.contains("2024-12-31"));
        assertTrue(content.contains("Page Total"));
    }

    @Test
    void generateTransactionReport_nullFields() {
        Transaction tx = Transaction.builder()
                .tranId(null)
                .tranTypeCd(null)
                .tranCatCd(null)
                .tranSource(null)
                .tranCardNum(null)
                .tranAmt(null)
                .build();

        when(transactionRepository.findAll()).thenReturn(List.of(tx));
        when(transactionTypeRepository.findAll()).thenReturn(List.of());
        when(transactionCategoryRepository.findAll()).thenReturn(List.of());

        byte[] report = reportService.generateTransactionReport(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertNotNull(report);
        assertTrue(report.length > 0);
    }
}
