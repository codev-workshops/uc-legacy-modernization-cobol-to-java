package com.carddemo.common.repository;

import com.carddemo.common.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByAcctId(Long acctId);
}
