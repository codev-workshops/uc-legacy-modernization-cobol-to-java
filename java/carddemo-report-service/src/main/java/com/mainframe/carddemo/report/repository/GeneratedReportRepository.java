package com.mainframe.carddemo.report.repository;

import com.mainframe.carddemo.report.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
}
