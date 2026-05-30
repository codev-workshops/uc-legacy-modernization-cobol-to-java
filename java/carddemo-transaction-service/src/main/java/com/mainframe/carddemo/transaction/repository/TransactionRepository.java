package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranCardNum(String tranCardNum);

    Page<Transaction> findByTranCardNumIn(Collection<String> cardNums, Pageable pageable);
}
