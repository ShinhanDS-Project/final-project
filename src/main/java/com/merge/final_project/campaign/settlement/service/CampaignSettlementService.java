package com.merge.final_project.campaign.settlement.service;

import com.merge.final_project.blockchain.service.SettlementTransactionService;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.campaign.settlement.dto.SelectSettlementResponseDTO;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignSettlementService {

    private final CampaignRepository campaignRepository;
    private final SettlementTransactionService settlementTransactionService;
    private final SettlementRepository settlementRepository;

    //    캠페인 조회
    public void settleAll() {
        List<Campaign> campaigns =
                campaignRepository.findByCampaignStatus(CampaignStatus.ENDED);

        for(Campaign campaign : campaigns) {
            try {
                settlementTransactionService.processSettlement(campaign);
            } catch (Exception e){
                log.error("정산 실패 campaignNo={}", campaign.getCampaignNo(), e);
            }
        }
    }

    //[이채원] 정산내역 조회
    public SelectSettlementResponseDTO showSettle(Long campaignNo){

        // 1.정산 목록 불러오기(여러개 뜨면 status가 성공인 경우에 )
        Settlement settlement = settlementRepository.findByCampaignNo(campaignNo).stream()
                .filter(s -> s.getStatus().equals(SettlementStatus.COMPLETED))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        // 2. 정산 성공한거 가져오면 return
        return SelectSettlementResponseDTO.builder()
                .foundationNo(settlement.getFoundation().getFoundationNo())
                .foundationAmount(settlement.getFoundationAmount())
                .settledAt(settlement.getSettledAt())
                .beneficiaryAmount(settlement.getBeneficiaryAmount())
                .beneficiaryNo(settlement.getBeneficiary().getBeneficiaryNo())
                .settlementStatus(settlement.getStatus())
                .build();

    }

}
