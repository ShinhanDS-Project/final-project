package com.merge.final_project.org;

import com.merge.final_project.org.dto.FoundationApplyRequestDTO;
import com.merge.final_project.org.dto.FoundationApplyResponseDTO;
import com.merge.final_project.org.dto.FoundationDetailResponseDTO;
import com.merge.final_project.org.dto.FoundationListResponseDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FoundationService {
    IllegalFoundationResponseDTO checkIllegalFoundation(FoundationApplyRequestDTO requestDTO);
    boolean existByBusinessRegistrationNumber(String businessRegistrationNumber);
    FoundationApplyResponseDTO apply(FoundationApplyRequestDTO requestDTO, MultipartFile profileImage);
    Page<FoundationListResponseDTO> getFoundationApplicationList(String reviewStatus, Pageable pageable);
    FoundationDetailResponseDTO getFoundationDetail(Long foundationNo);
}
