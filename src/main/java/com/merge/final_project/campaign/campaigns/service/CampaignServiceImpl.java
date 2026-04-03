package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.BeneficiaryResponseDto;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.UsePlanRepository;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.wallet.Wallet;
import com.merge.final_project.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CampaignServiceImpl implements CampaignService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final FoundationRepository foundationRepository;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final UsePlanRepository usePlanRepository;

    @Override
    @Transactional
    public BeneficiaryResponseDto verifyBeneficiary(Integer entryCode) {
        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new IllegalArgumentException("참여코드 없음"));

        return BeneficiaryResponseDto.builder()
                .name(beneficiary.getName())
                .phone(beneficiary.getPhone())
                .beneficiaryNo(beneficiary.getBeneficiaryNo())
                .build();
    }

    @Override
    @Transactional
    public void registerCampaign(CampaignRequestDto dto, Long foundationNo) {
        // 기부단체 지갑 주소 3개 가져오기
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("기부단체 정보를 찾을 수 없습니다."));

        List<String> walletAddresses = List.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        );

        // 사용 가능한 지갑 매칭
        Wallet availableWallet = walletRepository.findFirstByWalletAddressInAndStatus(walletAddresses, "INACTIVE")
                .orElseThrow(() -> new IllegalStateException("현재 사용 가능한 빈 지갑이 없습니다."));

        // 캠페인 엔티티 생성 및 지갑 매칭
        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo.intValue());
        campaign.setWalletNo(availableWallet.getWalletNo()); // 자동 매칭된 지갑 번호

        Campaign savedCampaign = campaignRepository.save(campaign);

        // 지출 계획 저장
        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        // 매칭된 지갑 상태 변경
        availableWallet.setStatus("ACTIVE");
    }
}
