package com.mainframe.carddemo.common.client;

import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.common.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "carddemo-account-service", url = "${feign.account-service.url:http://localhost:8082}")
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{accountId}")
    AccountDto getAccountById(@PathVariable("accountId") Long accountId);

    @GetMapping("/api/customers/{customerId}")
    CustomerDto getCustomerById(@PathVariable("customerId") Long customerId);

    @GetMapping("/api/accounts")
    List<AccountDto> getAllAccounts();

    @GetMapping("/internal/xref/{cardNum}")
    CardXrefDto getXrefByCardNum(@PathVariable("cardNum") String cardNum);

    @GetMapping("/internal/xref/byAccount/{acctId}")
    List<CardXrefDto> getXrefByAccountId(@PathVariable("acctId") Long acctId);

    @GetMapping("/internal/accounts/{id}")
    AccountDto getInternalAccountById(@PathVariable("id") Long id);

    @PutMapping("/internal/accounts/{id}/balance")
    AccountDto updateAccountBalance(@PathVariable("id") Long id, @RequestBody BalanceUpdateDto balanceUpdate);
}
