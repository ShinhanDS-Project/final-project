package com.merge.final_project.org;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.org.dto.*;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FoundationService {
    IllegalFoundationResponseDTO checkIllegalFoundation(FoundationApplyRequestDTO requestDTO);
    boolean existByBusinessRegistrationNumber(String businessRegistrationNumber);
    FoundationApplyResponseDTO apply(FoundationApplyRequestDTO requestDTO, MultipartFile profileImage);

    FoundationSigninResponseDTO login(FoundationSigninRequestDTO requestDTO);
    void logout(String bearerToken);

    FoundationDetailResponseDTO updateFoundationInfo(Long foundationNo, FoundationUpdateRequestDTO requestDTO, MultipartFile profileImage);
    void updateFoundationPassword(Long foundationNo, FoundationPasswordUpdateRequestDTO requestDTO);

    Page<FoundationListResponseDTO> getFoundationApplicationList(Pageable pageable);
    Page<FoundationListResponseDTO> getRejectedFoundationList(Pageable pageable);
    Page<FoundationListResponseDTO> getApprovedFoundationList(AccountStatus accountStatus, Pageable pageable);

    // [가빈] 관리자 신청/반려 목록 — 키워드 검색 추가
    Page<FoundationListResponseDTO> getFoundationApplicationListWithFilter(String keyword, Pageable pageable);
    Page<FoundationListResponseDTO> getRejectedFoundationListWithFilter(String keyword, Pageable pageable);

    // [가빈] 공개 단체 목록 — accountStatus 제거, 키워드 검색 추가
    Page<FoundationListResponseDTO> getPublicFoundationList(String keyword, Pageable pageable);

    // [가빈] 관리자 승인 단체 목록 — 상태 필터 + 키워드 검색 + 페이징
    Page<FoundationListResponseDTO> getApprovedFoundationListForAdmin(AccountStatus accountStatus, String keyword, Pageable pageable);
    FoundationDetailResponseDTO getFoundationDetail(Long foundationNo);

    Long approveFoundation(Long foundationNo);
    Long rejectFoundationForIllegal(Long foundationNo);

    // [가빈] 관리자 기부단체 활성화 (임시 비밀번호 발급 + 메일 발송)
    void activateFoundation(Long foundationNo);
    // [가빈] 관리자 기부단체 비활성화
    void deactivateFoundation(Long foundationNo);

    Page<CampaignListResponseDTO> getMyCampaigns(Long foundationNo, Pageable pageable);

    // [가빈] 기부단체 마이페이지 — 상태 필터 + 키워드 검색 캠페인 목록
    Page<FoundationMyCampaignDTO> getMyCampaignsWithFilter(Long foundationNo, CampaignStatus campaignStatus, String keyword, Pageable pageable);

    // [가빈] 기부단체 마이페이지 — 진행 중 캠페인 수 + 이번달 모금액
    FoundationMyPageStatsDTO getMyPageStats(Long foundationNo);
}
