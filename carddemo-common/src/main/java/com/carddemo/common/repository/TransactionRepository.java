package com.carddemo.common.repository;

import com.carddemo.common.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    Page<Transaction> findByCardNumIn(List<String> cardNums, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE "
            + "(:cardNum IS NULL OR t.cardNum = :cardNum) AND "
            + "(:startDate IS NULL OR t.origTs >= :startDate) AND "
            + "(:endDate IS NULL OR t.origTs <= :endDate)")
    Page<Transaction> findFiltered(@Param("cardNum") String cardNum,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.cardNum IN :cardNums AND "
            + "(:startDate IS NULL OR t.origTs >= :startDate) AND "
            + "(:endDate IS NULL OR t.origTs <= :endDate)")
    Page<Transaction> findByCardNumsFiltered(@Param("cardNums") List<String> cardNums,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             Pageable pageable);
}
