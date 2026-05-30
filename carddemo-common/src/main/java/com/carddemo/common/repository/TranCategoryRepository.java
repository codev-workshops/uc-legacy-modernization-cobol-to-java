package com.carddemo.common.repository;

import com.carddemo.common.entity.TranCategory;
import com.carddemo.common.entity.TranCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCategoryRepository extends JpaRepository<TranCategory, TranCategoryId> {
}
