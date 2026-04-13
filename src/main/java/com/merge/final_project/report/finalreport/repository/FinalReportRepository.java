package com.merge.final_project.report.finalreport.repository;

import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinalReportRepository extends JpaRepository<FinalReport, Long>, QuerydslPredicateExecutor<FinalReport> {

    @Query("SELECT f FROM FinalReport f JOIN Beneficiary b ON f.beneficiary_no = b.beneficiaryNo WHERE b.email = :email")
    List<FinalReport> findByBeneficiaryEmail(@Param("email") String email);

    //[가빈] 상태 별로 보고서 목록 조회해오려고 추가함
    Page<FinalReport> findByApprovalStatus(ReportApprovalStatus approvalStatus, Pageable pageable);

    //[채원] 캠페인 아이디 기준으로 FinalReport 조회
    Optional<FinalReport> findByCampaign_no(Long campaignNo);
}
