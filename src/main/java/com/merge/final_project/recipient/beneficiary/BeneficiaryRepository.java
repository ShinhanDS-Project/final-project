package com.merge.final_project.recipient.beneficiary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    // 참여코드로 수혜자 정보 조회(for 캠페인 등록)
    Optional<Beneficiary> findByEntryCode(Integer entryCode);
}
