package com.mainframe.carddemo.batch.repository;

import com.mainframe.carddemo.batch.entity.DisclosureGroup;
import com.mainframe.carddemo.batch.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
}
