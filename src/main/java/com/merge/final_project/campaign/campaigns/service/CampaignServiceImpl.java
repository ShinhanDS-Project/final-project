package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRegisterResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class CampaignServiceImpl implements CampaignService {
    private static final String CAMPAIGN_IMAGE_TARGET_NAME = "campaign";
    private static final String REPRESENTATIVE_IMAGE_PURPOSE = "REPRESENTATIVE";
    private static final String DETAIL_IMAGE_PURPOSE = "DETAIL";
    private static final List<CampaignStatus> LIST_VISIBLE_STATUSES = List.of(
        CampaignStatus.RECRUITING,
        CampaignStatus.ACTIVE,
        CampaignStatus.ENDED,
        CampaignStatus.SETTLED,
        CampaignStatus.COMPLETED,
        CampaignStatus.CANCELLED
    );

    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final UsePlanRepository usePlanRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher; // [가빈] SSE 이벤트 발행용

    @Override
    @Transactional
    public CampaignRegisterResponseDTO registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("대표 이미지는 필수입니다.");
        }

        Foundation foundation = foundationRepository.findById(foundationNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 재단 정보를 찾을 수 없습니다."));

        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(dto.getEntryCode())
            .orElseThrow(() -> new IllegalArgumentException("Invalid beneficiary entry code."));

        List<String> walletAddresses = Stream.of(
            foundation.getCampaignWallet1(),
            foundation.getCampaignWallet2(),
            foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        Wallet availableWallet = walletRepository
            .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
            .orElseThrow(() -> new IllegalStateException("사용 가능한 지갑이 없습니다."));

        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setBeneficiaryNo(beneficiary.getBeneficiaryNo());
        campaign.setWalletNo(availableWallet.getWalletNo());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING);
        campaign.setCampaignStatus(CampaignStatus.PENDING);
        campaign.setUpdatedAt(LocalDateTime.now());

        Campaign savedCampaign = campaignRepository.save(campaign);

        saveCampaignImage(imageFile, savedCampaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        availableWallet.changeStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);

        // [가빈] 관리자에게 캠페인 승인 요청 SSE 알림
        eventPublisher.publishEvent(new ApprovalRequestEvent(
                TargetType.CAMPAIGN,
                savedCampaign.getCampaignNo(),
                savedCampaign.getTitle() + " 캠페인 승인 요청"));

        return CampaignRegisterResponseDTO.builder()
            .campaignNo(savedCampaign.getCampaignNo())
            .foundationNo(savedCampaign.getFoundationNo())
            .approvalStatus(savedCampaign.getApprovalStatus() == null ? null : savedCampaign.getApprovalStatus().name())
            .campaignStatus(savedCampaign.getCampaignStatus() == null ? null : savedCampaign.getCampaignStatus().name())
            .message("캠페인 등록 요청 완료")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignListResponseDTO> getCampaignList(String sort, String searchType, String keyword, String category) {
        Comparator<Campaign> comparator;

        if ("participation".equalsIgnoreCase(sort)) {
            comparator = Comparator
                .comparing((Campaign campaign) -> campaign.getCurrentAmount() == null ? 0L : campaign.getCurrentAmount())
                .reversed()
                .thenComparing(Campaign::getCampaignNo, Comparator.reverseOrder());
        } else {
            comparator = Comparator
                .comparing(Campaign::getEndAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Campaign::getCampaignNo, Comparator.reverseOrder());
        }

        List<Campaign> campaigns = campaignRepository.findAll().stream()
            .filter(campaign -> campaign.getCampaignStatus() != null && LIST_VISIBLE_STATUSES.contains(campaign.getCampaignStatus()))
            .filter(campaign -> matchesCategory(campaign, category))
            .filter(campaign -> matchesKeyword(campaign, searchType, keyword))
            .sorted(comparator)
            .toList();

        return toCampaignListResponse(campaigns);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailResponseDTO getCampaignDetail(Long campaignNo) {
        Campaign campaign = campaignRepository.findByCampaignNoAndApprovalStatus(campaignNo, ApprovalStatus.APPROVED)
            .orElseThrow(() -> new IllegalArgumentException("승인된 캠페인을 찾을 수 없습니다."));

        Foundation foundation = foundationRepository.findByFoundationNo(campaign.getFoundationNo())
            .orElseThrow(() -> new IllegalArgumentException("기부 단체 정보를 찾을 수 없습니다."));

        long currentAmount = campaign.getCurrentAmount() == null ? 0L : campaign.getCurrentAmount();
        long targetAmount = campaign.getTargetAmount() == null ? 0L : campaign.getTargetAmount();
        CampaignStatus campaignStatus = campaign.getCampaignStatus() == null ? CampaignStatus.PENDING : campaign.getCampaignStatus();
        String walletAddress = campaign.getWalletNo() == null ? null : walletRepository.findById(campaign.getWalletNo())
            .map(Wallet::getWalletAddress)
            .orElse(null);

        List<Image> campaignImages = imageRepository.findByTargetNameAndTargetNo(CAMPAIGN_IMAGE_TARGET_NAME, campaignNo)
            .stream()
            .sorted(Comparator.comparing(Image::getCreatedAt))
            .toList();

        String representativeImagePath = campaignImages.stream()
            .filter(image -> REPRESENTATIVE_IMAGE_PURPOSE.equals(image.getPurpose()))
            .map(Image::getImgPath)
            .findFirst()
            .orElse(campaign.getImagePath());

        List<String> detailImagePaths = campaignImages.stream()
            .filter(image -> DETAIL_IMAGE_PURPOSE.equals(image.getPurpose()))
            .map(Image::getImgPath)
            .toList();

        List<CampaignDetailResponseDTO.UsePlanSummary> usePlans = usePlanRepository.findByCampaignNoOrderByUsePlanNoAsc(campaignNo)
            .stream()
            .map(plan -> CampaignDetailResponseDTO.UsePlanSummary.builder()
                .usePlanNo(plan.getUsePlanNo())
                .planContent(plan.getPlanContent())
                .planAmount(plan.getPlanAmount())
                .build())
            .toList();

        return CampaignDetailResponseDTO.builder()
            .campaignNo(campaign.getCampaignNo())
            .title(campaign.getTitle())
            .description(campaign.getDescription())
            .category(campaign.getCategory() == null ? null : campaign.getCategory().getLabel())
            .approvalStatus(campaign.getApprovalStatus() == null ? ApprovalStatus.PENDING.name() : campaign.getApprovalStatus().name())
            .campaignStatus(campaignStatus.name())
            .campaignStatusLabel(toCampaignStatusLabel(campaignStatus))
            .historyTitle(toHistoryTitle(campaignStatus))
            .historyDescription(toHistoryDescription(campaignStatus))
            .targetAmount(targetAmount)
            .currentAmount(currentAmount)
            .progressPercent(calculateProgressPercent(currentAmount, targetAmount))
            .remainingAmount(calculateRemainingAmount(targetAmount, currentAmount))
            .daysLeft(calculateDaysLeft(campaign.getEndAt()))
            .startAt(campaign.getStartAt())
            .endAt(campaign.getEndAt())
            .usageStartAt(campaign.getUsageStartAt())
            .usageEndAt(campaign.getUsageEndAt())
            .walletAddress(walletAddress)
            .representativeImagePath(representativeImagePath)
            .detailImagePaths(detailImagePaths)
            .foundation(CampaignDetailResponseDTO.FoundationSummary.builder()
                .foundationNo(foundation.getFoundationNo())
                .foundationName(foundation.getFoundationName())
                .description(foundation.getDescription())
                .profilePath(foundation.getProfilePath())
                .build())
            .usePlans(usePlans)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignBeneficiaryCheckResponseDTO checkBeneficiaryByEntryCode(String entryCode) {
        return beneficiaryRepository.findByEntryCode(entryCode)
            .map(beneficiary -> CampaignBeneficiaryCheckResponseDTO.builder()
                .valid(true)
                .beneficiaryNo(beneficiary.getBeneficiaryNo())
                .entryCode(beneficiary.getEntryCode())
                .name(beneficiary.getName())
                .beneficiaryType(beneficiary.getBeneficiaryType() == null ? null : beneficiary.getBeneficiaryType().name())
                .message("수혜자 정보를 확인했습니다.")
                .build())
            .orElseGet(() -> CampaignBeneficiaryCheckResponseDTO.builder()
                .valid(false)
                .entryCode(entryCode)
                .message("일치하는 수혜자 entry code가 없습니다.")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignFoundationCheckResponseDTO checkFoundationWalletStatus(Long foundationNo) {
        Foundation foundation = foundationRepository.findByFoundationNo(foundationNo)
            .orElseThrow(() -> new IllegalArgumentException("기부 단체 정보를 찾을 수 없습니다."));

        List<CampaignFoundationCheckResponseDTO.WalletStatusItem> wallets = List.of(
            toWalletStatusItem("지갑 1", foundation.getCampaignWallet1()),
            toWalletStatusItem("지갑 2", foundation.getCampaignWallet2()),
            toWalletStatusItem("지갑 3", foundation.getCampaignWallet3())
        );

        boolean hasAvailableWallet = wallets.stream().anyMatch(CampaignFoundationCheckResponseDTO.WalletStatusItem::isAvailable);

        return CampaignFoundationCheckResponseDTO.builder()
            .foundationNo(foundation.getFoundationNo())
            .foundationName(foundation.getFoundationName())
            .hasAvailableWallet(hasAvailableWallet)
            .message(hasAvailableWallet ? "사용 가능한 캠페인 지갑이 있습니다." : "사용 가능한 캠페인 지갑이 없습니다.")
            .wallets(wallets)
            .build();
    }

    private List<CampaignListResponseDTO> toCampaignListResponse(List<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            return List.of();
        }

        Map<Long, String> imagePathByCampaignNo = campaigns.stream()
            .collect(Collectors.toMap(
                Campaign::getCampaignNo,
                campaign -> imageRepository.findByTargetNameAndTargetNo(
                        CAMPAIGN_IMAGE_TARGET_NAME,
                        campaign.getCampaignNo()
                    ).stream()
                    .filter(image -> REPRESENTATIVE_IMAGE_PURPOSE.equals(image.getPurpose()))
                    .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                    .map(Image::getImgPath)
                    .findFirst()
                    .orElse(campaign.getImagePath() == null ? "" : campaign.getImagePath())
            ));

        return campaigns.stream()
            .map(campaign -> CampaignListResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .foundationNo(campaign.getFoundationNo())
                .imagePath(imagePathByCampaignNo.get(campaign.getCampaignNo()))
                .title(campaign.getTitle())
                .foundationName(
                    campaign.getFoundationNo() == null
                        ? null
                        : foundationRepository.findByFoundationNo(campaign.getFoundationNo())
                            .map(Foundation::getFoundationName)
                            .orElse(null)
                )
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .category(toCampaignCategoryLabel(campaign.getCategory()))
                .endAt(campaign.getEndAt())
                .build())
            .toList();
    }

    private boolean matchesCategory(Campaign campaign, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }

        try {
            CampaignCategory campaignCategory = CampaignCategory.valueOf(category.trim().toUpperCase());
            return campaignCategory.equals(campaign.getCategory());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean matchesKeyword(Campaign campaign, String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        if ("foundation".equalsIgnoreCase(searchType)) {
            String foundationName = campaign.getFoundationNo() == null
                ? null
                : foundationRepository.findByFoundationNo(campaign.getFoundationNo())
                    .map(Foundation::getFoundationName)
                    .orElse(null);
            return foundationName != null && foundationName.toLowerCase().contains(normalizedKeyword);
        }

        String title = campaign.getTitle();
        return title != null && title.toLowerCase().contains(normalizedKeyword);
    }

    private String toCampaignCategoryLabel(CampaignCategory category) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case CHILD_YOUTH -> "아동/청소년";
            case SENIOR -> "어르신";
            case DISABLED -> "장애인";
            case ANIMAL -> "동물";
            case ENVIRONMENT -> "환경";
            case ETC -> "기타";
        };
    }

    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            saveCampaignImage(detailImageFile, campaignNo, DETAIL_IMAGE_PURPOSE);
        }
    }

    private void saveCampaignImage(MultipartFile imageFile, Long campaignNo, String purpose) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        try {
            String storedName = fileService.saveFile(imageFile);
            String filePath = fileService.getFilePath(storedName);

            imageRepository.save(Image.builder()
                .imgPath(filePath)
                .imgOrgName(imageFile.getOriginalFilename())
                .imgStoredName(storedName)
                .targetName(CAMPAIGN_IMAGE_TARGET_NAME)
                .targetNo(campaignNo)
                .createdAt(LocalDateTime.now())
                .purpose(purpose)
                .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CampaignFoundationCheckResponseDTO.WalletStatusItem toWalletStatusItem(String walletLabel, String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            return CampaignFoundationCheckResponseDTO.WalletStatusItem.builder()
                .walletLabel(walletLabel)
                .walletAddress(null)
                .status("NOT_REGISTERED")
                .available(false)
                .build();
        }

        Optional<Wallet> wallet = walletRepository.findByWalletAddress(walletAddress);
        String status = wallet.map(value -> value.getStatus() == null ? "UNKNOWN" : value.getStatus().name()).orElse("NOT_FOUND");
        boolean available = wallet.map(value -> WalletStatus.INACTIVE.equals(value.getStatus())).orElse(false);

        return CampaignFoundationCheckResponseDTO.WalletStatusItem.builder()
            .walletLabel(walletLabel)
            .walletAddress(walletAddress)
            .status(status)
            .available(available)
            .build();
    }

    private int calculateProgressPercent(Long currentAmount, Long targetAmount) {
        if (targetAmount == null || targetAmount <= 0) {
            return 0;
        }

        long safeCurrentAmount = currentAmount == null ? 0L : currentAmount;
        return (int) Math.min(100, (safeCurrentAmount * 100) / targetAmount);
    }

    private long calculateRemainingAmount(Long targetAmount, Long currentAmount) {
        long safeTargetAmount = targetAmount == null ? 0L : targetAmount;
        long safeCurrentAmount = currentAmount == null ? 0L : currentAmount;
        return Math.max(0L, safeTargetAmount - safeCurrentAmount);
    }

    private long calculateDaysLeft(LocalDateTime endAt) {
        if (endAt == null) {
            return 0L;
        }

        LocalDateTime now = LocalDateTime.now();
        if (endAt.isBefore(now)) {
            return 0L;
        }

        return Math.max(0L, Duration.between(now, endAt).toDays());
    }

    private String toCampaignStatusLabel(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "승인 대기";
            case RECRUITING -> "모금 준비 중";
            case ACTIVE -> "나눔 진행 중";
            case ENDED -> "정산 준비 중";
            case SETTLED -> "정산 완료";
            case COMPLETED -> "나눔 완료";
            case CANCELLED -> "나눔 취소";
        };
    }

    private String toHistoryTitle(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "캠페인 승인 대기";
            case RECRUITING -> "모금 준비 중";
            case ACTIVE -> "나눔 진행 중";
            case ENDED -> "정산 준비 중";
            case SETTLED -> "정산 완료";
            case COMPLETED -> "나눔 완료";
            case CANCELLED -> "캠페인 취소";
        };
    }

    private String toHistoryDescription(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "캠페인이 승인 대기 상태입니다. 운영진 검토가 끝나면 모금 일정이 공개됩니다.";
            case RECRUITING -> "모금 시작 전 준비 단계입니다. 공개 일정이 시작되면 상세 내역이 순차적으로 반영됩니다.";
            case ACTIVE -> "현재 모금이 진행 중이거나 정산 준비 단계에 있습니다. 정산이 완료되면 상세 내역이 여기에 표시됩니다.";
            case ENDED -> "모금은 종료되었으며 정산을 준비하고 있습니다. 정산이 확정되면 나눔 내역을 확인할 수 있습니다.";
            case SETTLED -> "정산이 완료되었습니다. 최종 보고와 사용 결과가 정리되면 이 영역에서 확인할 수 있습니다.";
            case COMPLETED -> "나눔과 보고가 모두 완료된 캠페인입니다. 최종 집행 결과가 반영된 상태입니다.";
            case CANCELLED -> "캠페인이 취소되었습니다. 진행 사유와 상태는 운영 정책에 따라 별도로 안내됩니다.";
        };
    }
}
