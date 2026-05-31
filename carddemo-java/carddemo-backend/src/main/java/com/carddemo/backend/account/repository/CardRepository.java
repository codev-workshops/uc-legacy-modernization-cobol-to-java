package com.carddemo.backend.account.repository;

import com.carddemo.backend.account.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, String> {

    Page<CardEntity> findByCardAcctId(Long cardAcctId, Pageable pageable);
}
