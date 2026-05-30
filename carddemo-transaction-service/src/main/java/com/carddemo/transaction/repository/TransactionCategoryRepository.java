package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {

    List<TransactionCategory> findByTranTypeCd(String tranTypeCd);
}
