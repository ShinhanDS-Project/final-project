package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.*;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
            CampaignStatus.APPROVED,
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
    // [바다] additional detail payload sources
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher; // SSE 이벤트 발행을 위한 주입

    @Override
    @Transactional
    public CampaignRegisterResponseDTO registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("대표 이미지는 필수입니다.");
        }

        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 재단 정보를 찾을 수 없습니다."));

        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(dto.getEntryCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 수혜자 코드입니다."));

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
        campaign.setCurrentAmount(BigDecimal.valueOf(0)); //채원 수정
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

        // 관리자에게 캠페인 승인 요청 SSE 알림 발행
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
    @Transactional
    public CampaignRegisterResponseDTO updatePendingCampaign(Long campaignNo, CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));

        validatePendingEditableCampaign(campaign, foundationNo);

        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(dto.getEntryCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 수혜자 코드입니다."));

        campaign.setTitle(dto.getTitle());
        campaign.setDescription(dto.getDescription());
        campaign.setCategory(dto.getCategory());
        campaign.setTargetAmount(dto.getTargetAmount());
        campaign.setStartAt(dto.getStartAt());
        campaign.setEndAt(dto.getEndAt());
        campaign.setUsageStartAt(dto.getUsageStartAt());
        campaign.setUsageEndAt(dto.getUsageEndAt());
        campaign.setBeneficiaryNo(beneficiary.getBeneficiaryNo());
        campaign.setUpdatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            saveCampaignImage(imageFile, campaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);
        }
        deleteDetailImages(dto.getDeletedDetailImageNos(), campaign.getCampaignNo());
        saveDetailImages(detailImageFiles, campaign.getCampaignNo());

        usePlanRepository.deleteByCampaignNo(campaign.getCampaignNo());
        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> usePlanRepository.save(planDto.toEntity(campaign.getCampaignNo())));
        }

        return CampaignRegisterResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .foundationNo(campaign.getFoundationNo())
                .approvalStatus(campaign.getApprovalStatus() == null ? null : campaign.getApprovalStatus().name())
                .campaignStatus(campaign.getCampaignStatus() == null ? null : campaign.getCampaignStatus().name())
                .message("캠페인 수정이 완료되었습니다.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignListResponseDTO> getCampaignList(String sort, String searchType, String keyword, String category) {
        Comparator<Campaign> comparator;

        if ("participation".equalsIgnoreCase(sort)) {
            comparator = Comparator
                    .comparing((Campaign campaign) -> campaign.getCurrentAmount() == null ? BigDecimal.ZERO : campaign.getCurrentAmount())
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

        return toCampaignDetailResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailResponseDTO getPendingCampaignForEdit(Long campaignNo, Long foundationNo) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));

        validatePendingEditableCampaign(campaign, foundationNo);

        return toCampaignDetailResponse(campaign);
    }

    private CampaignDetailResponseDTO toCampaignDetailResponse(Campaign campaign) {
        Foundation foundation = foundationRepository.findByFoundationNo(campaign.getFoundationNo())
                .orElseThrow(() -> new IllegalArgumentException("기부 단체 정보를 찾을 수 없습니다."));

        BigDecimal currentAmount = campaign.getCurrentAmount() == null ? BigDecimal.ZERO : campaign.getCurrentAmount();
        long targetAmount = campaign.getTargetAmount() == null ? 0L : campaign.getTargetAmount();
        CampaignStatus campaignStatus = campaign.getCampaignStatus() == null ? CampaignStatus.PENDING : campaign.getCampaignStatus();
        // [바다] donors count for detail cards
        long donors = donationRepository.countByCampaignNo(campaign.getCampaignNo());
        String walletAddress = campaign.getWalletNo() == null ? null : walletRepository.findById(campaign.getWalletNo())
                .map(Wallet::getWalletAddress)
                .orElse(null);

        List<Image> campaignImages = imageRepository.findByTargetNameAndTargetNo(CAMPAIGN_IMAGE_TARGET_NAME, campaign.getCampaignNo())
                .stream()
                .sorted(Comparator.comparing(Image::getCreatedAt))
                .toList();

        String representativeImagePath = campaignImages.stream()
                .filter(image -> REPRESENTATIVE_IMAGE_PURPOSE.equals(image.getPurpose()))
                .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                .map(Image::getImgPath)
                .findFirst()
                .orElse(campaign.getImagePath());

        List<String> detailImagePaths = campaignImages.stream()
                .filter(image -> DETAIL_IMAGE_PURPOSE.equals(image.getPurpose()))
                .map(Image::getImgPath)
                .toList();

        List<CampaignDetailResponseDTO.ImageSummary> images = campaignImages.stream()
                .map(image -> CampaignDetailResponseDTO.ImageSummary.builder()
                        .imgNo(image.getImgNo())
                        .imgPath(image.getImgPath())
                        .imgOrgName(image.getImgOrgName())
                        .imgStoredName(image.getImgStoredName())
                        .purpose(image.getPurpose())
                        .build())
                .toList();

        List<CampaignDetailResponseDTO.UsePlanSummary> usePlans = usePlanRepository.findByCampaignNoOrderByUsePlanNoAsc(campaign.getCampaignNo())
                .stream()
                .map(plan -> CampaignDetailResponseDTO.UsePlanSummary.builder()
                        .usePlanNo(plan.getUsePlanNo())
                        .planContent(plan.getPlanContent())
                        .planAmount(plan.getPlanAmount())
                        .build())
                .toList();

        // [바다] beneficiary tab payload
        Beneficiary beneficiary = beneficiaryRepository.findById(campaign.getBeneficiaryNo()).orElse(null);
        CampaignDetailResponseDTO.BeneficiarySummary beneficiarySummary = CampaignDetailResponseDTO.BeneficiarySummary.builder()
                .title(beneficiary == null ? "수혜자 정보 준비 중" : beneficiary.getName())
                .target(beneficiary == null || beneficiary.getBeneficiaryType() == null
                        ? "-"
                        : beneficiary.getBeneficiaryType().name())
                .build();

        // [바다] recent donor tab payload
        List<CampaignDetailResponseDTO.RecentDonorSummary> recentDonors = donationRepository
                .findTop5ByCampaignNoOrderByDonatedAtDesc(campaign.getCampaignNo())
                .stream()
                .map(this::toRecentDonorSummary)
                .toList();

        // [바다] documents tab payload (minimal from use plans)
        List<CampaignDetailResponseDTO.DocumentSummary> documents = usePlans.stream()
                .map(plan -> CampaignDetailResponseDTO.DocumentSummary.builder()
                        .name(plan.getPlanContent() == null || plan.getPlanContent().isBlank()
                                ? "사용 계획서"
                                : plan.getPlanContent())
                        .size(plan.getPlanAmount() == null ? "-" : String.format("%,d원", plan.getPlanAmount()))
                        .href("")
                        .build())
                .toList();

        return CampaignDetailResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .category(campaign.getCategory() == null ? null : campaign.getCategory().getLabel())
                .categoryCode(campaign.getCategory() == null ? null : campaign.getCategory().name())
                .entryCode(findEntryCode(campaign.getBeneficiaryNo()))
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
                .donors(donors)
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .usageStartAt(campaign.getUsageStartAt())
                .usageEndAt(campaign.getUsageEndAt())
                .walletAddress(walletAddress)
                .representativeImagePath(representativeImagePath)
                .detailImagePaths(detailImagePaths)
                .images(images)
                .foundation(CampaignDetailResponseDTO.FoundationSummary.builder()
                        .foundationNo(foundation.getFoundationNo())
                        .foundationName(foundation.getFoundationName())
                        .description(foundation.getDescription())
                        .profilePath(foundation.getProfilePath())
                        .build())
                .usePlans(usePlans)
                .beneficiary(beneficiarySummary)
                .recentDonors(recentDonors)
                .documents(documents)
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
                        .message("일치하는 수혜자 코드가 없습니다.")
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

    private void validatePendingEditableCampaign(Campaign campaign, Long foundationNo) {
        if (!Objects.equals(campaign.getFoundationNo(), foundationNo)) {
            throw new IllegalArgumentException("해당 재단의 캠페인만 조회할 수 있습니다.");
        }

        boolean isPendingCampaign =
                ApprovalStatus.PENDING.equals(campaign.getApprovalStatus())
                        && CampaignStatus.PENDING.equals(campaign.getCampaignStatus());

        boolean isRejectedCampaign =
                ApprovalStatus.REJECTED.equals(campaign.getApprovalStatus());

        if (!isPendingCampaign && !isRejectedCampaign) {
            throw new IllegalStateException("승인 대기 또는 반려 상태의 캠페인만 조회할 수 있습니다.");
        }
    }

    private String findEntryCode(Long beneficiaryNo) {
        if (beneficiaryNo == null) {
            return null;
        }

        return beneficiaryRepository.findById(beneficiaryNo)
                .map(Beneficiary::getEntryCode)
                .orElse(null);
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

    // [바다] mapper for recent donor payload
    private CampaignDetailResponseDTO.RecentDonorSummary toRecentDonorSummary(Donation donation) {
        String donorName;
        if (donation.isAnonymous()) {
            donorName = "익명";
        } else {
            donorName = userRepository.findByUserNo(donation.getUserNo())
                    .map(User::getName)
                    .orElse("기부자");
        }

        long amount = donation.getDonationAmount() == null ? 0L : donation.getDonationAmount().longValue();
        return CampaignDetailResponseDTO.RecentDonorSummary.builder()
                .name(donorName)
                .amount(amount)
                .time(toRelativeTime(donation.getDonatedAt()))
                .build();
    }

    // [바다] relative time formatter for recent donors
    private String toRelativeTime(LocalDateTime donatedAt) {
        if (donatedAt == null) {
            return "-";
        }
        Duration duration = Duration.between(donatedAt, LocalDateTime.now());
        long minutes = Math.max(0L, duration.toMinutes());
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;
        return days + "일 전";
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
            case CHILD_YOUTH -> "아동 및 청소년";
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

    private void deleteDetailImages(List<Long> imageNos, Long campaignNo) {
        if (imageNos == null || imageNos.isEmpty()) {
            return;
        }

        imageRepository.findAllById(imageNos).stream()
                .filter(image -> CAMPAIGN_IMAGE_TARGET_NAME.equals(image.getTargetName()))
                .filter(image -> Objects.equals(campaignNo, image.getTargetNo()))
                .filter(image -> DETAIL_IMAGE_PURPOSE.equals(image.getPurpose()))
                .forEach(image -> {
                    fileService.deleteFile(image.getImgStoredName());
                    imageRepository.delete(image);
                });
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
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    private CampaignFoundationCheckResponseDTO.WalletStatusItem toWalletStatusItem(String walletLabel, String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            return CampaignFoundationCheckResponseDTO.WalletStatusItem.builder()
                    .walletLabel(walletLabel)
                    .walletAddress(null)
                    .status("미등록")
                    .available(false)
                    .build();
        }

        Optional<Wallet> wallet = walletRepository.findByWalletAddress(walletAddress);
        String status = wallet.map(
                value -> value.getStatus() == null ? "상태불명" : value.getStatus().name()).orElse("찾을수없음");
        boolean available = wallet.map(value -> WalletStatus.INACTIVE.equals(value.getStatus())).orElse(false);

        return CampaignFoundationCheckResponseDTO.WalletStatusItem.builder()
                .walletLabel(walletLabel)
                .walletAddress(walletAddress)
                .status(status)
                .available(available)
                .build();
    }

    private int calculateProgressPercent(BigDecimal currentAmount, Long targetAmount) {
        if (targetAmount == null || targetAmount <= 0) {
            return 0;
        }

        BigDecimal safeCurrentAmount = currentAmount == null ? BigDecimal.ZERO : currentAmount;
        //BigDecimal은 *,/연산자 사용 불가라서 수정함.
        return safeCurrentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(targetAmount), RoundingMode.DOWN)
                .min(BigDecimal.valueOf(100))
                .intValue();
    }

    private long calculateRemainingAmount(Long targetAmount, BigDecimal currentAmount) {
        long safeTargetAmount = targetAmount == null ? 0L : targetAmount;
        BigDecimal safeCurrentAmount = currentAmount == null ? BigDecimal.ZERO : currentAmount;
        return Math.max(0L, safeTargetAmount - safeCurrentAmount.longValue());
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
            case PENDING -> "승인 대기중";
            case APPROVED, RECRUITING -> "모집중";
            case ACTIVE -> "진행중";
            case ENDED -> "모집 종료";
            case SETTLED -> "정산 완료";
            case COMPLETED -> "캠페인 종료";
            case CANCELLED -> "취소됨";
        };
    }

    private String toHistoryTitle(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "검토 중인 캠페인";
            case APPROVED, RECRUITING -> "참여 가능한 캠페인";
            case ACTIVE -> "기부금이 전달된 캠페인";
            case ENDED -> "모집이 완료된 캠페인";
            case SETTLED -> "정산이 보고된 캠페인";
            case COMPLETED -> "모든 일정이 종료된 캠페인";
            case CANCELLED -> "중단된 캠페인";
        };
    }

    private String toHistoryDescription(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "관리자가 캠페인을 검토하고 있습니다.";
            case APPROVED, RECRUITING -> "기부 참여가 활발히 이루어지고 있습니다.";
            case ACTIVE -> "목표 금액이 달성되어 기부금이 수혜자에게 전달되었습니다.";
            case ENDED -> "모집 기간이 종료되어 정산을 준비 중입니다.";
            case SETTLED -> "기부금 사용 내역이 투명하게 공개되었습니다.";
            case COMPLETED -> "성공적으로 캠페인이 마무리되었습니다.";
            case CANCELLED -> "부득이한 사정으로 캠페인이 취소되었습니다.";
        };
    }
}
