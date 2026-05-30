package com.carddemo.common.client;

import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.dto.CardXrefDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", url = "${carddemo.account-service.url:http://localhost:8082}")
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{id}")
    AccountDto getAccount(@PathVariable Long id);

    @PutMapping("/api/accounts/{id}")
    AccountDto updateAccount(@PathVariable Long id, @RequestBody AccountDto account);

    @GetMapping("/api/card-xref/{cardNum}")
    CardXrefDto getCardXref(@PathVariable String cardNum);

    @GetMapping("/api/cards/{cardNum}")
    CardDto getCard(@PathVariable String cardNum);
}
