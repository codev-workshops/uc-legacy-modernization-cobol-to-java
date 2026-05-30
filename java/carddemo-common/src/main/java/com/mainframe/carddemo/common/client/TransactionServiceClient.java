package com.mainframe.carddemo.common.client;

import com.mainframe.carddemo.common.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "carddemo-transaction-service", url = "${feign.transaction-service.url:http://localhost:8083}")
public interface TransactionServiceClient {

    @GetMapping("/api/transactions/{transactionId}")
    TransactionDto getTransactionById(@PathVariable("transactionId") String transactionId);

    @GetMapping("/api/transactions/card/{cardNum}")
    List<TransactionDto> getTransactionsByCardNum(@PathVariable("cardNum") String cardNum);
}
