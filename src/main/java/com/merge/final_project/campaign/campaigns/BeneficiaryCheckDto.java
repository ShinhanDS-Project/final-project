package com.merge.final_project.campaign.campaigns;

import com.merge.final_project.recipient.beneficiary.Beneficiary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public class BeneficiaryCheckDto extends JpaRepository<Beneficiary, Integer> {
    // 수혜자 참여코드로 조회 시, 연관된 지갑(wallet)을 한 번에 가져옴 (N+1 문제 방지)
    @EntityGraph(attributePaths = {"wallet"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Beneficiary> findByEntryCode(Integer entryCode);
}
