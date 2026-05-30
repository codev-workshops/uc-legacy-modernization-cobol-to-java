package com.carddemo.online.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.online.dto.CardDetailResponse;
import com.carddemo.online.dto.CardResponse;
import com.carddemo.online.dto.CardUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CardService cardService;

    private Card sampleCard;
    private CardXref sampleXref;
    private Account sampleAccount;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCard = new Card();
        sampleCard.setCardNum("4111111111111111");
        sampleCard.setAcctId(1001L);
        sampleCard.setCvvCd(123);
        sampleCard.setEmbossedName("JOHN DOE");
        sampleCard.setExpirationDate("2025-12-31");
        sampleCard.setActiveStatus("Y");

        sampleXref = new CardXref();
        sampleXref.setXrefCardNum("4111111111111111");
        sampleXref.setAcctId(1001L);
        sampleXref.setCustId(5001L);

        sampleAccount = new Account();
        sampleAccount.setAcctId(1001L);
        sampleAccount.setActiveStatus("Y");
        sampleAccount.setCurrBal(new BigDecimal("5000.00"));
        sampleAccount.setCreditLimit(new BigDecimal("10000.00"));
        sampleAccount.setExpirationDate("2026-12-31");

        sampleCustomer = new Customer();
        sampleCustomer.setCustId(5001L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");
    }

    @Test
    void listCards_returnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(sampleCard), pageable, 1);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        Page<CardResponse> result = cardService.listCards(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("4111111111111111", result.getContent().get(0).getCardNum());
        assertEquals(1001L, result.getContent().get(0).getAcctId());
    }

    @Test
    void listCards_emptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(), pageable, 0);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        Page<CardResponse> result = cardService.listCards(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getCard_withXref_returnsDetail() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleXref));
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(sampleAccount));
        when(customerRepository.findById(5001L)).thenReturn(Optional.of(sampleCustomer));

        CardDetailResponse detail = cardService.getCard("4111111111111111");

        assertEquals("4111111111111111", detail.getCardNum());
        assertEquals("JOHN DOE", detail.getEmbossedName());
        assertNotNull(detail.getAccount());
        assertEquals(1001L, detail.getAccount().getAcctId());
        assertEquals(new BigDecimal("5000.00"), detail.getAccount().getCurrBal());
        assertNotNull(detail.getCustomer());
        assertEquals(5001L, detail.getCustomer().getCustId());
        assertEquals("John", detail.getCustomer().getFirstName());
    }

    @Test
    void getCard_withoutXref_returnsDetailNoAccountCustomer() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.empty());

        CardDetailResponse detail = cardService.getCard("4111111111111111");

        assertEquals("4111111111111111", detail.getCardNum());
        assertNull(detail.getAccount());
        assertNull(detail.getCustomer());
        verify(accountRepository, never()).findById(any());
        verify(customerRepository, never()).findById(any());
    }

    @Test
    void getCard_notFound_throwsException() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(CardService.CardNotFoundException.class,
                () -> cardService.getCard("9999999999999999"));
    }

    @Test
    void updateCard_allFields_updatesAndReturns() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = new CardUpdateRequest();
        request.setAcctId(2002L);
        request.setCvvCd(456);
        request.setEmbossedName("JANE DOE");
        request.setExpirationDate("2027-06-30");
        request.setActiveStatus("N");

        CardResponse response = cardService.updateCard("4111111111111111", request);

        assertEquals("JANE DOE", response.getEmbossedName());
        assertEquals("N", response.getActiveStatus());
        assertEquals(2002L, response.getAcctId());
        assertEquals(456, response.getCvvCd());
        verify(cardRepository).save(sampleCard);
    }

    @Test
    void updateCard_partialFields_updatesOnlyProvided() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("UPDATED NAME");

        CardResponse response = cardService.updateCard("4111111111111111", request);

        assertEquals("UPDATED NAME", response.getEmbossedName());
        assertEquals(1001L, response.getAcctId());
        assertEquals(123, response.getCvvCd());
        assertEquals("Y", response.getActiveStatus());
    }

    @Test
    void updateCard_notFound_throwsException() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("TEST");

        assertThrows(CardService.CardNotFoundException.class,
                () -> cardService.updateCard("9999999999999999", request));
    }
}
