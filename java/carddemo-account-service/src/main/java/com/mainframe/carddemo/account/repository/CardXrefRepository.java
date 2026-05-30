package com.mainframe.carddemo.account.repository;

import com.mainframe.carddemo.account.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefAcctId(Long xrefAcctId);

    List<CardXref> findByXrefCustId(Long xrefCustId);
}
