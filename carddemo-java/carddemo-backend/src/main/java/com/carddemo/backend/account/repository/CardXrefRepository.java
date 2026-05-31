package com.carddemo.backend.account.repository;

import com.carddemo.backend.account.entity.CardXrefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXrefEntity, String> {

    Optional<CardXrefEntity> findByXrefCardNum(String xrefCardNum);

    List<CardXrefEntity> findByXrefAcctId(Long xrefAcctId);
}
