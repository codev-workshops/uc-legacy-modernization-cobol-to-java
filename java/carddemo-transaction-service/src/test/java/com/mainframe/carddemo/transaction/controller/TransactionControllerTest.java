package com.mainframe.carddemo.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.transaction.entity.Transaction;
import com.mainframe.carddemo.transaction.service.BillPaymentRequest;
import com.mainframe.carddemo.transaction.service.TransactionCreateRequest;
import com.mainframe.carddemo.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listTransactions_returnsPage() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("0000000000000001");
        txn.setTranTypeCd("01");
        txn.setTranAmt(new BigDecimal("100.00"));
        txn.setTranCardNum("4111111111111111");
        Page<Transaction> page = new PageImpl<>(List.of(txn));

        when(transactionService.getTransactionsByAccount(eq(1L), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/transactions")
                        .param("acctId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("0000000000000001"));
    }

    @Test
    void getTransaction_returnsTransaction() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("ABC123");
        txn.setTranAmt(new BigDecimal("50.00"));

        when(transactionService.getTransactionById("ABC123")).thenReturn(txn);

        mockMvc.perform(get("/api/transactions/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("ABC123"));
    }

    @Test
    void createTransaction_returnsCreated() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("NEW123");
        txn.setTranTypeCd("01");
        txn.setTranAmt(new BigDecimal("200.00"));

        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(txn);

        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNum("4111111111111111");
        request.setTranTypeCd("01");
        request.setTranCatCd(1);
        request.setTranAmt(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("NEW123"));
    }

    @Test
    void billPayment_returnsCreated() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("PAY123");
        txn.setTranTypeCd("02");
        txn.setTranAmt(new BigDecimal("-100.00"));

        when(transactionService.processBillPayment(any(BillPaymentRequest.class))).thenReturn(txn);

        BillPaymentRequest request = new BillPaymentRequest();
        request.setCardNum("4111111111111111");
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/billing/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("PAY123"));
    }
}
