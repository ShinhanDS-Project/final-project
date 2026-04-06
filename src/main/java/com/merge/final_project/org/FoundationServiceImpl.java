package com.merge.final_project.org;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.util.Upload;
import com.merge.final_project.org.dto.FoundationApplyRequestDTO;
import com.merge.final_project.org.dto.FoundationApplyResponseDTO;
import com.merge.final_project.org.dto.FoundationDetailResponseDTO;
import com.merge.final_project.org.dto.FoundationListResponseDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundation;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationRepository;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationServiceImpl implements FoundationService {

    private final FoundationRepository foundationRepository;
    private final IllegalFoundationRepository illegalFoundationRepository;
    private final Upload upload;    //프로필 이미지는 위한 업로드 필드 추가


    @Override
    public IllegalFoundationResponseDTO checkIllegalFoundation(FoundationApplyRequestDTO requestDTO) {
        //정확하게 일치하는 단체가 있는지,
        Optional<IllegalFoundation> exactMatch = illegalFoundationRepository.findByNameAndRepresentative(
                requestDTO.getFoundationName(), requestDTO.getRepresentativeName());
        //혹은 유사한 단체가 있는지
        List<IllegalFoundation> similarList = illegalFoundationRepository.findByNameContaining(requestDTO.getFoundationName());
        //정확하게 일치하지 않는 경우로만 이루어지도록 리스트 필터링.
        List<IllegalFoundation> filteredSimilar = similarList.stream()
                .filter(f -> exactMatch.isEmpty() || !f.getIllegalNo().equals(exactMatch.get().getIllegalNo()))
                .toList();

        return IllegalFoundationResponseDTO.builder()
                .exactMatch(exactMatch.isPresent())
                .matchedFoundation(exactMatch.
                        map(IllegalFoundationDTO::from)
                        .orElse(null))
                .similarFoundations(filteredSimilar.stream()
                        .map(IllegalFoundationDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public boolean existByBusinessRegistrationNumber(String businessRegistrationNumber) {
        return foundationRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber);
    }

    @Override
    public FoundationApplyResponseDTO apply(FoundationApplyRequestDTO requestDTO, MultipartFile profileImage) {

        if (foundationRepository.existsByBusinessRegistrationNumber(requestDTO.getBusinessRegistrationNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION);
        }

        //불법 단체 체크
        IllegalFoundationResponseDTO checkedFoundation = checkIllegalFoundation(requestDTO);

        ReviewStatus reviewStatus;
        if (checkedFoundation.isExactMatch()) {
            reviewStatus = ReviewStatus.ILLEGAL;
        } else if (!checkedFoundation.getSimilarFoundations().isEmpty()) {
            reviewStatus = ReviewStatus.SIMILAR;
        } else {
            reviewStatus = ReviewStatus.CLEAN;
        }

        //프로필 사진
        String profilePath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profilePath = upload.store(profileImage);
            } catch (IOException e) {
                log.error("프로필 이미지 저장 실패", e);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        // foundation 엔티티 빌드 후 저장. (임시 비밀번호 없는 상태)
        Foundation foundation = Foundation.builder()
                .foundationEmail(requestDTO.getFoundationEmail())
                .foundationPassword(null)
                .foundationName(requestDTO.getFoundationName())
                .businessRegistrationNumber(requestDTO.getBusinessRegistrationNumber())
                .representativeName(requestDTO.getRepresentativeName())
                .description(requestDTO.getDescription())
                .profilePath(profileImage.isEmpty()
                        ? null
                        : profilePath)
                .contactPhone(requestDTO.getContactPhone())
                .account(requestDTO.getAccount())
                .feeRate(requestDTO.getFeeRate())
                .foundationHash(null)
                .accountStatus("INACTIVE")
                .reviewStatus(reviewStatus)
                .rejectReason(checkedFoundation.isExactMatch()
                        ? checkedFoundation.getMatchedFoundation().getReason()
                        : null)
                .campaignWallet1(null)
                .campaignWallet2(null)
                .campaignWallet3(null)
                .bankName(requestDTO.getBankName())
                .account(requestDTO.getAccount())
                .walletNo(null)
                .foundationType(requestDTO.getFoundationType())
                .build();

        foundationRepository.save(foundation);
        return FoundationApplyResponseDTO.builder()
                .foundationEmail(foundation.getFoundationEmail())
                .foundationName(foundation.getFoundationName())
                .representativeName(foundation.getRepresentativeName())
                .build();
    }

    @Override
    public Page<FoundationListResponseDTO> getFoundationApplicationList(String reviewStatus, Pageable pageable) {
        // 신청 해놓고 승인되지 않은 기부단체들을 볼 수 있는 목록
        return null;
    }

    @Override
    public FoundationDetailResponseDTO getFoundationDetail(Long foundationNo) {
        //기부 단체 리뷰 상태에 따라 다른 정보 표시 할 것.
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));
        return FoundationDetailResponseDTO.from(foundation);
    }

    //기부 리뷰 상태 변경하는 메서드 구현 (승인 시 임시 비밀번호 생성)-> 메일 보내는 메서드 비동기로 호출,
    //반려 시 반려 사유를 담아 메일 보내는 메서드 비동기로 호출

    //
}
