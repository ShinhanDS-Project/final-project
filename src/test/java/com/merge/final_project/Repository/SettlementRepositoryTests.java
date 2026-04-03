package com.merge.final_project.Repository;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.Campaign;
import com.merge.final_project.campaign.campaigns.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
public class SettlementRepositoryTests {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private FoundationRepository foundationRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    @Autowired
    private CampaignRepository campaignRepository;

    @Test
    public void insertWallet() {

        Key key3 = Key.builder()
                .privateKey("dummy-private-key-3")
                .aesKey("dummy-aes-key-3")
                .build();

        keyRepository.save(key3);

        Key key4 = Key.builder()
                .privateKey("dummy-private-key-4")
                .aesKey("dummy-aes-key-4")
                .build();

        keyRepository.save(key4);

        Wallet beneficiaryWallet = Wallet.builder()
                .walletType(WalletType.BENEFICIARY)
                .key(key3)
                .ownerNo(null)
                .walletAddress("0xbeneficiary123")
                .balance(new BigDecimal("0"))
                .walletHash("hash_beneficiary_123")
                .status(WalletStatus.ACTIVE)
                .campaign(null)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(beneficiaryWallet);

        Wallet foundationWallet = Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .key(key4)
                .ownerNo(null)
                .walletAddress("0xfoundation123")
                .balance(new BigDecimal("0"))
                .walletHash("hash_foundation_123")
                .status(WalletStatus.ACTIVE)
                .campaign(null)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(foundationWallet);

    }

    @Test
    public void insertBeneficiary() {
        Wallet beneficiaryWallet = walletRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("수혜자 지갑 없음"));

        Beneficiary beneficiary = Beneficiary.builder()
                .name("희망아동센터")
                .email("beneficiary@test.com")
                .password("1234")
                .phone("010-1111-2222")
                .account("110-123-456789")
                .beneficiaryHash("hash_beneficiary_001")
                .beneficiaryType(BeneficiaryType.INDIVIDUAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .wallet(beneficiaryWallet)
                .entryCode(11111)
                .build();

        beneficiaryRepository.save(beneficiary);
    }

    @Test
    public void insertFoundation() {
        Wallet foundationWallet = walletRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("기부단체 지갑 없음"));

        Foundation foundation = Foundation.builder()
                .foundationHash("hash_foundation_001")
                .foundationName("굿네이버재단")
                .businessRegistrationNumber(1234654)
                .representativeName("홍길동")
                .contactPhone("02-1234-5678")
                .description("아동 및 취약계층 지원 단체")
                .feeRate(new BigDecimal("0.05"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profilePath("/images/foundation1.png")
                .account("222-333-444444")
                .foundationEmail("foundation@test.com")
                .wallet(foundationWallet)
                .build();

        foundationRepository.save(foundation);
    }

    @Test
    public void createCampaignWithWallet() {

        Key key = keyRepository.save(
                Key.builder()
                        .privateKey("campaign-private-key")
                        .build()
        );

        Wallet campaignWallet = walletRepository.save(
                Wallet.builder()
                        .walletType(WalletType.CAMPAIGN)
                        .key(key)
                        .walletAddress("0xcampaign_wallet_003")
                        .balance(BigDecimal.ZERO)
                        .walletHash("hash_campaign_wallet_001")
                        .status(WalletStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Campaign campaign = Campaign.builder()
                .title("테스트 캠페인")
                .description("DB 맞춘 캠페인")

                .imagePath("test.png")

                .startAt("2026-04-03 10:00:00")
                .endAt("2026-05-03 10:00:00")
                .usageStartAt("2026-05-04 10:00:00")
                .usageEndAt("2026-06-04 10:00:00")

                .targetAmount("1000000")
                .currentAmount("0")
                .achievedAt(null)

                .category("DONATION")

                .approvalStatus(ApprovalStatus.APPROVED)
                .approvedAt("2026-04-03 10:00:00")

                .campaignStatus(CampaignStatus.ACTIVE)

                // 🔥 핵심 (String으로 변환)
                .walletNo(String.valueOf(campaignWallet.getWalletNo()))
                .foundationNo("1")
                .beneficiaryNo("1")

                .createdAt("2026-04-03 10:00:00")
                .updatedAt("2026-04-03 10:00:00")

                .rejectReason(null)

                .build();

        campaignRepository.save(campaign);

        System.out.println("캠페인 + 지갑 생성 완료 🔥");
    }



}
