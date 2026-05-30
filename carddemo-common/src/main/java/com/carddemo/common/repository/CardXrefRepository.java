package com.carddemo.common.repository;

import com.carddemo.common.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByAcctId(Long acctId);
    List<CardXref> findByCustId(Long custId);
    List<CardXref> findByAcctIdIn(Set<Long> acctIds);
}
