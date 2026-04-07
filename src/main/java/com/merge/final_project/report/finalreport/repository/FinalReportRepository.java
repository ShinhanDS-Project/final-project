package com.merge.final_project.report.finalreport.repository;

import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FinalReportRepository extends JpaRepository<FinalReport, Long>, QuerydslPredicateExecutor<FinalReport> {
    @Query("SELECT f FROM FinalReport f JOIN Beneficiary b ON f.beneficiary_no = b.beneficiaryNo WHERE b.email = :email")
    List<FinalReport> findByBeneficiaryEmail(@Param("email") String email);
}
