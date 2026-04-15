package com.merge.final_project.org;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.org.dto.*;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FoundationService {
    //불법 단체인지 확인
    IllegalFoundationResponseDTO checkIllegalFoundation(FoundationApplyRequestDTO requestDTO);
    //사업자 번호 중복 조회
    boolean existByBusinessRegistrationNumber(String businessRegistrationNumber);
    //신청하기
    FoundationApplyResponseDTO apply(FoundationApplyRequestDTO requestDTO, MultipartFile profileImage);

    //로그인 로그아웃
    FoundationSigninResponseDTO login(FoundationSigninRequestDTO requestDTO);
    void logout(String bearerToken);

    // 회원 정보 수정
    FoundationDetailResponseDTO updateFoundationInfo(Long foundationNo, FoundationUpdateRequestDTO requestDTO, MultipartFile profileImage);
    void updateFoundationPassword(Long foundationNo, FoundationPasswordUpdateRequestDTO requestDTO);

    // 관리자 신청/반려 목록 — 키워드 검색 추가
    Page<FoundationListResponseDTO> getFoundationApplicationListWithFilter(ReviewStatus reviewStatus,String keyword, Pageable pageable);
    Page<FoundationListResponseDTO> getRejectedFoundationListWithFilter(String keyword, Pageable pageable);

    //공개 단체 목록 — 키워드 검색 추가 (여기는 상태가 승인된 애들만 공개 됨)
    Page<FoundationListResponseDTO> getPublicFoundationList(String keyword, Pageable pageable);

    //관리자 승인 단체 목록 — 상태 필터 + 키워드 검색 + 페이징
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

    // [가빈] 기부단체 마이페이지 — 지갑 주소 + 잔액
    FoundationWalletDTO getMyWalletInfo(Long foundationNo);

    // [가빈] 기부단체 마이페이지 — 정산 내역
    Page<FoundationSettlementDTO> getMySettlements(Long foundationNo, Pageable pageable);

    // [가빈] 기부단체 마이페이지 — 환금(현금화) 내역
    Page<FoundationRedemptionDTO> getMyRedemptions(Long foundationNo, Pageable pageable);

}
