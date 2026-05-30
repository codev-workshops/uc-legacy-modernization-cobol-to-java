package com.carddemo.batch.job;

import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.TranCategory;
import com.carddemo.common.entity.TranCategoryId;
import com.carddemo.common.entity.TranType;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TranCategoryRepository;
import com.carddemo.common.repository.TranTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionReportProcessorTest {

    @Mock
    private CardXrefRepository cardXrefRepo;
    @Mock
    private TranTypeRepository tranTypeRepo;
    @Mock
    private TranCategoryRepository tranCatRepo;

    private TransactionReportProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionReportProcessor(cardXrefRepo, tranTypeRepo, tranCatRepo);
    }

    @Test
    void process_enrichesWithLookups() {
        Transaction tx = buildTransaction("TX001", "1111111111111111", "SA", 5001,
                new BigDecimal("100.00"), "ONLINE");

        CardXref xref = new CardXref();
        xref.setXrefCardNum("1111111111111111");
        xref.setAcctId(1L);
        when(cardXrefRepo.findById("1111111111111111")).thenReturn(Optional.of(xref));

        TranType type = new TranType();
        type.setTranType("SA");
        type.setTranTypeDesc("Sale");
        when(tranTypeRepo.findById("SA")).thenReturn(Optional.of(type));

        TranCategory cat = new TranCategory();
        cat.setTypeCd("SA");
        cat.setCatCd(5001);
        cat.setTranCatTypeDesc("Online Purchase");
        when(tranCatRepo.findById(any(TranCategoryId.class))).thenReturn(Optional.of(cat));

        TransactionReportItem item = processor.process(tx);

        assertNotNull(item);
        assertEquals("TX001", item.getTranId());
        assertEquals("00000000001", item.getAccountId());
        assertEquals("SA", item.getTypeCd());
        assertEquals("Sale", item.getTypeDesc());
        assertEquals(5001, item.getCatCd());
        assertEquals("Online Purchase", item.getCatDesc());
        assertEquals("ONLINE", item.getSource());
        assertEquals(new BigDecimal("100.00"), item.getAmount());
        assertEquals("1111111111111111", item.getCardNum());
    }

    @Test
    void process_missingXref_setsNA() {
        Transaction tx = buildTransaction("TX002", "9999999999999999", "SA", 5001,
                new BigDecimal("50.00"), "POS");

        when(cardXrefRepo.findById("9999999999999999")).thenReturn(Optional.empty());
        when(tranTypeRepo.findById("SA")).thenReturn(Optional.empty());
        when(tranCatRepo.findById(any(TranCategoryId.class))).thenReturn(Optional.empty());

        TransactionReportItem item = processor.process(tx);

        assertNotNull(item);
        assertEquals("N/A", item.getAccountId());
        assertEquals("", item.getTypeDesc());
        assertEquals("", item.getCatDesc());
    }

    @Test
    void process_nullCardNum() {
        Transaction tx = buildTransaction("TX003", null, null, null,
                new BigDecimal("10.00"), null);

        when(cardXrefRepo.findById("")).thenReturn(Optional.empty());
        when(tranTypeRepo.findById("")).thenReturn(Optional.empty());
        when(tranCatRepo.findById(any(TranCategoryId.class))).thenReturn(Optional.empty());

        TransactionReportItem item = processor.process(tx);

        assertNotNull(item);
        assertEquals("N/A", item.getAccountId());
        assertEquals(0, item.getCatCd());
    }

    private Transaction buildTransaction(String id, String cardNum, String typeCd,
                                         Integer catCd, BigDecimal amt, String source) {
        Transaction tx = new Transaction();
        tx.setTranId(id);
        tx.setCardNum(cardNum);
        tx.setTypeCd(typeCd);
        tx.setCatCd(catCd);
        tx.setAmt(amt);
        tx.setSource(source);
        return tx;
    }
}
