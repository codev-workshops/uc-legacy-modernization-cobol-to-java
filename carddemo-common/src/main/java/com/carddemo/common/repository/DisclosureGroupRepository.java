package com.carddemo.common.repository;

import com.carddemo.common.entity.DisclosureGroup;
import com.carddemo.common.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
}
