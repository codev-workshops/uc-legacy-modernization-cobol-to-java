package com.carddemo.authorization.service;

import com.carddemo.authorization.dto.AuthorizationDto;
import com.carddemo.authorization.entity.AuthFraud;
import com.carddemo.authorization.entity.Authorization;
import com.carddemo.authorization.mapper.AuthorizationMapper;
import com.carddemo.authorization.repository.AuthFraudRepository;
import com.carddemo.authorization.repository.AuthorizationRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final AuthorizationRepository authorizationRepository;
    private final AuthFraudRepository authFraudRepository;
    private final AuthorizationMapper authorizationMapper;

    public AuthorizationService(AuthorizationRepository authorizationRepository,
                                AuthFraudRepository authFraudRepository,
                                AuthorizationMapper authorizationMapper) {
        this.authorizationRepository = authorizationRepository;
        this.authFraudRepository = authFraudRepository;
        this.authorizationMapper = authorizationMapper;
    }

    public List<AuthorizationDto> getAuthorizationSummary(String cardNum) {
        log.debug("Fetching authorization summary for card: {}", cardNum);
        List<Authorization> auths = authorizationRepository.findByCardNum(cardNum);
        return authorizationMapper.toDtoList(auths);
    }

    public Page<AuthorizationDto> getAuthorizationSummaryPaged(String cardNum, int page, int size) {
        log.debug("Fetching paged authorization summary for card: {}", cardNum);
        Page<Authorization> authPage = authorizationRepository.findByCardNum(cardNum, PageRequest.of(page, size));
        return authPage.map(authorizationMapper::toDto);
    }

    public AuthorizationDto getAuthorizationDetail(Integer authId) {
        log.debug("Fetching authorization detail for ID: {}", authId);
        Authorization auth = authorizationRepository.findById(authId)
                .orElseThrow(() -> new ResourceNotFoundException("Authorization not found with ID: " + authId));
        return authorizationMapper.toDto(auth);
    }

    @Transactional
    public AuthorizationDto markAsFraud(Integer authId) {
        log.info("Marking authorization {} as fraud", authId);
        Authorization auth = authorizationRepository.findById(authId)
                .orElseThrow(() -> new ResourceNotFoundException("Authorization not found with ID: " + authId));

        AuthFraud fraud = AuthFraud.builder()
                .cardNum(auth.getCardNum())
                .authTs(auth.getAuthTs())
                .authType(auth.getAuthType())
                .cardExpiryDate(auth.getCardExpiryDate())
                .messageType(auth.getMessageType())
                .messageSource(auth.getMessageSource())
                .authIdCode(auth.getAuthIdCode())
                .authRespCode(auth.getAuthRespCode())
                .authRespReason(auth.getAuthRespReason())
                .processingCode(auth.getProcessingCode())
                .transactionAmt(auth.getTransactionAmt())
                .approvedAmt(auth.getApprovedAmt())
                .merchantCategoryCode(auth.getMerchantCategoryCode())
                .acqrCountryCode(auth.getAcqrCountryCode())
                .posEntryMode(auth.getPosEntryMode())
                .merchantId(auth.getMerchantId())
                .merchantName(auth.getMerchantName())
                .merchantCity(auth.getMerchantCity())
                .merchantState(auth.getMerchantState())
                .merchantZip(auth.getMerchantZip())
                .transactionId(auth.getTransactionId())
                .matchStatus(auth.getMatchStatus())
                .authFraudFlag("Y")
                .fraudRptDate(LocalDate.now())
                .acctId(auth.getAcctId())
                .custId(auth.getCustId())
                .build();

        authFraudRepository.save(fraud);
        log.info("Authorization {} copied to auth_fraud table", authId);
        return authorizationMapper.toDto(auth);
    }

    public Authorization saveAuthorization(Authorization authorization) {
        return authorizationRepository.save(authorization);
    }
}
