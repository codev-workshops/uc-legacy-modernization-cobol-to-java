package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.DisclosureGroup;
import com.mainframe.carddemo.transaction.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
}
