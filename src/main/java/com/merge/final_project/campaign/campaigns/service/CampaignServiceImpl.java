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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class CampaignServiceImpl implements CampaignService {
    private static final String CAMPAIGN_IMAGE_TARGET_NAME = "campaign";
    private static final String REPRESENTATIVE_IMAGE_PURPOSE = "REPRESENTATIVE";
    private static final String DETAIL_IMAGE_PURPOSE = "DETAIL";
    private static final int DEFAULT_CAMPAIGN_PAGE_SIZE = 6;
    private static final int MAX_CAMPAIGN_PAGE_SIZE = 30;
    private static final List<CampaignStatus> PUBLIC_ACTIVE_STATUSES = List.of(CampaignStatus.ACTIVE);
    private static final List<CampaignStatus> PUBLIC_ACTIVE_AND_CLOSED_STATUSES = List.of(
            CampaignStatus.ACTIVE,
            CampaignStatus.ENDED,
            CampaignStatus.SETTLED,
            CampaignStatus.COMPLETED
    );

    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final UsePlanRepository usePlanRepository;
    // [諛붾떎] additional detail payload sources
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher; // SSE ?대깽??諛쒗뻾???꾪븳 二쇱엯

    @Override
    @Transactional
    public CampaignRegisterResponseDTO registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("????대?吏???꾩닔?낅땲??");
        }

        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("?대떦 ?щ떒 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));

        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(dto.getEntryCode())
                .orElseThrow(() -> new IllegalArgumentException("?좏슚?섏? ?딆? ?섑삙??肄붾뱶?낅땲??"));

        List<String> walletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        Wallet availableWallet = walletRepository
                .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
                .orElseThrow(() -> new IllegalStateException("?ъ슜 媛?ν븳 吏媛묒씠 ?놁뒿?덈떎."));

        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setBeneficiaryNo(beneficiary.getBeneficiaryNo());
        campaign.setWalletNo(availableWallet.getWalletNo());
        campaign.setCurrentAmount(BigDecimal.valueOf(0)); //梨꾩썝 ?섏젙
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

        // 愿由ъ옄?먭쾶 罹좏럹???뱀씤 ?붿껌 SSE ?뚮┝ 諛쒗뻾
        eventPublisher.publishEvent(new ApprovalRequestEvent(
                TargetType.CAMPAIGN,
                savedCampaign.getCampaignNo(),
                savedCampaign.getTitle() + " 罹좏럹???뱀씤 ?붿껌"));

        return CampaignRegisterResponseDTO.builder()
                .campaignNo(savedCampaign.getCampaignNo())
                .foundationNo(savedCampaign.getFoundationNo())
                .approvalStatus(savedCampaign.getApprovalStatus() == null ? null : savedCampaign.getApprovalStatus().name())
                .campaignStatus(savedCampaign.getCampaignStatus() == null ? null : savedCampaign.getCampaignStatus().name())
                .message("罹좏럹???깅줉 ?붿껌 ?꾨즺")
                .build();
    }

    @Override
    @Transactional
    public CampaignRegisterResponseDTO updatePendingCampaign(Long campaignNo, CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new IllegalArgumentException("罹좏럹?몄쓣 李얠쓣 ???놁뒿?덈떎."));

        validatePendingEditableCampaign(campaign, foundationNo);

        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(dto.getEntryCode())
                .orElseThrow(() -> new IllegalArgumentException("?좏슚?섏? ?딆? ?섑삙??肄붾뱶?낅땲??"));

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
                .message("罹좏럹???섏젙???꾨즺?섏뿀?듬땲??")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignListPageResponseDTO getCampaignList(
            int page,
            int size,
            String sort,
            String keyword,
            String category,
            boolean includeClosed
    ) {
        Pageable pageable = createCampaignListPageable(page, size, sort);
        CampaignCategory campaignCategory = parseCategory(category);
        if (isInvalidCategory(category, campaignCategory)) {
            return emptyCampaignPageResponse(pageable);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        List<CampaignStatus> targetStatuses = includeClosed
                ? PUBLIC_ACTIVE_AND_CLOSED_STATUSES
                : PUBLIC_ACTIVE_STATUSES;

        Page<Campaign> campaignPage = campaignRepository.findPublicCampaignPage(
                ApprovalStatus.APPROVED,
                targetStatuses,
                campaignCategory,
                normalizedKeyword,
                pageable
        );

        return toCampaignListPageResponse(campaignPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailResponseDTO getCampaignDetail(Long campaignNo) {
        Campaign campaign = campaignRepository.findByCampaignNoAndApprovalStatus(campaignNo, ApprovalStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException("?뱀씤??罹좏럹?몄쓣 李얠쓣 ???놁뒿?덈떎."));

        return toCampaignDetailResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailResponseDTO getPendingCampaignForEdit(Long campaignNo, Long foundationNo) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new IllegalArgumentException("罹좏럹?몄쓣 李얠쓣 ???놁뒿?덈떎."));

        validatePendingEditableCampaign(campaign, foundationNo);

        return toCampaignDetailResponse(campaign);
    }

    private CampaignDetailResponseDTO toCampaignDetailResponse(Campaign campaign) {
        Foundation foundation = foundationRepository.findByFoundationNo(campaign.getFoundationNo())
                .orElseThrow(() -> new IllegalArgumentException("湲곕? ?⑥껜 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));

        BigDecimal currentAmount = campaign.getCurrentAmount() == null ? BigDecimal.ZERO : campaign.getCurrentAmount();
        long targetAmount = campaign.getTargetAmount() == null ? 0L : campaign.getTargetAmount();
        CampaignStatus campaignStatus = campaign.getCampaignStatus() == null ? CampaignStatus.PENDING : campaign.getCampaignStatus();
        // [諛붾떎] donors count for detail cards
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

        // [諛붾떎] beneficiary tab payload
        Beneficiary beneficiary = beneficiaryRepository.findById(campaign.getBeneficiaryNo()).orElse(null);
        CampaignDetailResponseDTO.BeneficiarySummary beneficiarySummary = CampaignDetailResponseDTO.BeneficiarySummary.builder()
                .title(beneficiary == null ? "수혜자 정보 준비 중" : beneficiary.getName())
                .target(beneficiary == null || beneficiary.getBeneficiaryType() == null
                        ? "-"
                        : beneficiary.getBeneficiaryType().name())
                .build();

        // [諛붾떎] recent donor tab payload
        List<CampaignDetailResponseDTO.RecentDonorSummary> recentDonors = donationRepository
                .findTop5ByCampaignNoOrderByDonatedAtDesc(campaign.getCampaignNo())
                .stream()
                .map(this::toRecentDonorSummary)
                .toList();

        // [諛붾떎] documents tab payload (minimal from use plans)
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
                        .message("?섑삙???뺣낫瑜??뺤씤?덉뒿?덈떎.")
                        .build())
                .orElseGet(() -> CampaignBeneficiaryCheckResponseDTO.builder()
                        .valid(false)
                        .entryCode(entryCode)
                        .message("?쇱튂?섎뒗 ?섑삙??肄붾뱶媛 ?놁뒿?덈떎.")
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignFoundationCheckResponseDTO checkFoundationWalletStatus(Long foundationNo) {
        Foundation foundation = foundationRepository.findByFoundationNo(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("湲곕? ?⑥껜 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));

        List<CampaignFoundationCheckResponseDTO.WalletStatusItem> wallets = List.of(
                toWalletStatusItem("吏媛?1", foundation.getCampaignWallet1()),
                toWalletStatusItem("吏媛?2", foundation.getCampaignWallet2()),
                toWalletStatusItem("吏媛?3", foundation.getCampaignWallet3())
        );

        boolean hasAvailableWallet = wallets.stream().anyMatch(CampaignFoundationCheckResponseDTO.WalletStatusItem::isAvailable);

        return CampaignFoundationCheckResponseDTO.builder()
                .foundationNo(foundation.getFoundationNo())
                .foundationName(foundation.getFoundationName())
                .hasAvailableWallet(hasAvailableWallet)
                .message(hasAvailableWallet ? "?ъ슜 媛?ν븳 罹좏럹??吏媛묒씠 ?덉뒿?덈떎." : "?ъ슜 媛?ν븳 罹좏럹??吏媛묒씠 ?놁뒿?덈떎.")
                .wallets(wallets)
                .build();
    }

    private void validatePendingEditableCampaign(Campaign campaign, Long foundationNo) {
        if (!Objects.equals(campaign.getFoundationNo(), foundationNo)) {
            throw new IllegalArgumentException("?대떦 ?щ떒??罹좏럹?몃쭔 議고쉶?????덉뒿?덈떎.");
        }

        boolean isPendingCampaign =
                ApprovalStatus.PENDING.equals(campaign.getApprovalStatus())
                        && CampaignStatus.PENDING.equals(campaign.getCampaignStatus());

        boolean isRejectedCampaign =
                ApprovalStatus.REJECTED.equals(campaign.getApprovalStatus());

        if (!isPendingCampaign && !isRejectedCampaign) {
            throw new IllegalStateException("?뱀씤 ?湲??먮뒗 諛섎젮 ?곹깭??罹좏럹?몃쭔 議고쉶?????덉뒿?덈떎.");
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

        List<Long> campaignNos = campaigns.stream()
                .map(Campaign::getCampaignNo)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, String> imagePathByCampaignNo = new HashMap<>();
        imageRepository.findByTargetNameAndPurposeAndTargetNoInOrderByTargetNoAscCreatedAtDesc(
                        CAMPAIGN_IMAGE_TARGET_NAME,
                        REPRESENTATIVE_IMAGE_PURPOSE,
                        campaignNos
                ).forEach(image -> imagePathByCampaignNo.putIfAbsent(image.getTargetNo(), image.getImgPath()));

        return campaigns.stream()
                .map(campaign -> CampaignListResponseDTO.builder()
                        .campaignNo(campaign.getCampaignNo())
                        .foundationNo(campaign.getFoundationNo())
                        .imagePath(imagePathByCampaignNo.getOrDefault(
                                campaign.getCampaignNo(),
                                campaign.getImagePath() == null ? "" : campaign.getImagePath()
                        ))
                        .title(campaign.getTitle())
                        .foundationName(campaign.getFoundation() == null ? null : campaign.getFoundation().getFoundationName())
                        .targetAmount(campaign.getTargetAmount())
                        .currentAmount(campaign.getCurrentAmount())
                        .category(toCampaignCategoryLabel(campaign.getCategory()))
                        .campaignStatus(campaign.getCampaignStatus())
                        .startAt(campaign.getStartAt())
                        .endAt(campaign.getEndAt())
                        .build())
                .toList();
    }

    // [諛붾떎] mapper for recent donor payload
    private CampaignDetailResponseDTO.RecentDonorSummary toRecentDonorSummary(Donation donation) {
        String donorName;
        if (donation.isAnonymous()) {
            donorName = "?듬챸";
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

    // [諛붾떎] relative time formatter for recent donors
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

    private CampaignCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return CampaignCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("invalid campaign category filter={}", category);
            return null;
        }
    }

    private boolean isInvalidCategory(String category, CampaignCategory campaignCategory) {
        return category != null && !category.isBlank() && campaignCategory == null;
    }

    private CampaignListPageResponseDTO emptyCampaignPageResponse(Pageable pageable) {
        return CampaignListPageResponseDTO.builder()
                .content(List.of())
                .pageInfo(CampaignListPageInfoDTO.builder()
                        .page(pageable.getPageNumber() + 1)
                        .size(pageable.getPageSize())
                        .totalElements(0)
                        .totalPages(1)
                        .hasNext(false)
                        .last(true)
                        .build())
                .build();
    }

    private CampaignListPageResponseDTO toCampaignListPageResponse(Page<Campaign> campaignPage) {
        return CampaignListPageResponseDTO.builder()
                .content(toCampaignListResponse(campaignPage.getContent()))
                .pageInfo(CampaignListPageInfoDTO.builder()
                        .page(campaignPage.getNumber() + 1)
                        .size(campaignPage.getSize())
                        .totalElements(campaignPage.getTotalElements())
                        .totalPages(Math.max(1, campaignPage.getTotalPages()))
                        .hasNext(campaignPage.hasNext())
                        .last(campaignPage.isLast())
                        .build())
                .build();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private Pageable createCampaignListPageable(int page, int size, String sort) {
        int safePage = Math.max(0, page - 1);
        int safeSize = sanitizeCampaignPageSize(size);
        if ("participation".equalsIgnoreCase(sort)) {
            return PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by(Sort.Order.desc("currentAmount"), Sort.Order.desc("campaignNo"))
            );
        }
        return PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.asc("endAt"), Sort.Order.desc("campaignNo"))
        );
    }

    private int sanitizeCampaignPageSize(int size) {
        if (size <= 0) {
            return DEFAULT_CAMPAIGN_PAGE_SIZE;
        }
        return Math.min(size, MAX_CAMPAIGN_PAGE_SIZE);
    }

    private String toCampaignCategoryLabel(CampaignCategory category) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case CHILD_YOUTH -> "아동/청소년";
            case SENIOR -> "노인";
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
            throw new RuntimeException("?대?吏 ???以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", e);
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
                value -> value.getStatus() == null ? "상태불명" : value.getStatus().name()).orElse("찾을 수 없음");
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
        //BigDecimal? *,/?곗궛???ъ슜 遺덇??쇱꽌 ?섏젙??
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
            case PENDING -> "?뱀씤 ?湲곗쨷";
            case APPROVED, RECRUITING -> "모집중";
            case ACTIVE -> "진행중";
            case ENDED -> "紐⑥쭛 醫낅즺";
            case SETTLED -> "?뺤궛 ?꾨즺";
            case COMPLETED -> "罹좏럹??醫낅즺";
            case CANCELLED -> "취소됨";
        };
    }

    private String toHistoryTitle(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "검토 중인 캠페인";
            case APPROVED, RECRUITING -> "참여 가능한 캠페인";
            case ACTIVE -> "기부금이 전달된 캠페인";
            case ENDED -> "모집이 완료된 캠페인";
            case SETTLED -> "정산 보고 캠페인";
            case COMPLETED -> "모든 일정이 종료된 캠페인";
            case CANCELLED -> "중단된 캠페인";
        };
    }

    private String toHistoryDescription(CampaignStatus campaignStatus) {
        return switch (campaignStatus) {
            case PENDING -> "愿由ъ옄媛 罹좏럹?몄쓣 寃?좏븯怨??덉뒿?덈떎.";
            case APPROVED, RECRUITING -> "湲곕? 李몄뿬媛 ?쒕컻???대（?댁?怨??덉뒿?덈떎.";
            case ACTIVE -> "紐⑺몴 湲덉븸???ъ꽦?섏뼱 湲곕?湲덉씠 ?섑삙?먯뿉寃??꾨떖?섏뿀?듬땲??";
            case ENDED -> "紐⑥쭛 湲곌컙??醫낅즺?섏뼱 ?뺤궛??以鍮?以묒엯?덈떎.";
            case SETTLED -> "湲곕?湲??ъ슜 ?댁뿭???щ챸?섍쾶 怨듦컻?섏뿀?듬땲??";
            case COMPLETED -> "?깃났?곸쑝濡?罹좏럹?몄씠 留덈Т由щ릺?덉뒿?덈떎.";
            case CANCELLED -> "遺?앹씠???ъ젙?쇰줈 罹좏럹?몄씠 痍⑥냼?섏뿀?듬땲??";
        };
    }
}
