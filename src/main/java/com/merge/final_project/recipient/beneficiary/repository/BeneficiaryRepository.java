package com.merge.final_project.recipient.beneficiary.repository;

import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    boolean existsByEmail(String email);

    Optional<Beneficiary> findByEmail(String email);

    Optional<Beneficiary> findByEntryCode(String entryCode);

    // [가빈] 전체 공지용 - 전체 수혜자 PK 목록 (상태 필드 없으므로 전체 대상)
    @Query("SELECT b.beneficiaryNo FROM Beneficiary b")
    List<Long> findAllBeneficiaryNos();
}
