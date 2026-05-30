package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.TranCategory;
import com.mainframe.carddemo.transaction.entity.TranCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCategoryRepository extends JpaRepository<TranCategory, TranCategoryId> {
}
