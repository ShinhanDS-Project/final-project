package com.merge.final_project.org;

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
    Page<FoundationListResponseDTO> getRejectedFoundationList( Pageable pageable);
    Page<FoundationListResponseDTO> getApprovedFoundationList(AccountStatus accountStatus, Pageable pageable);
    FoundationDetailResponseDTO getFoundationDetail(Long foundationNo);

    Long approveFoundation(Long foundationNo);
    Long rejectFoundationForIllegal(Long foundationNo);

    Page<CampaignListResponseDTO> getMyCampaigns(Long foundationNo, Pageable pageable);
}
