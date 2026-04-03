package com.merge.final_project.report.finalreport.repository;

import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface FinalReportRepository extends JpaRepository<FinalReport, Long>, QuerydslPredicateExecutor<FinalReport> {
}
