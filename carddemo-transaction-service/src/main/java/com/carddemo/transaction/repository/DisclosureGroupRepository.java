package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.DisclosureGroup;
import com.carddemo.transaction.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    Optional<DisclosureGroup> findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
            String acctGroupId, String tranTypeCd, Integer tranCatCd);
}
