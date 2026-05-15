package com.carddemo.repository;

import com.carddemo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCardNumber(String cardNumber);

    List<Transaction> findByTypeCode(String typeCode);
}
