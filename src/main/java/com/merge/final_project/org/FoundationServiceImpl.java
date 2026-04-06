package com.merge.final_project.org;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationServiceImpl implements FoundationService {

    private final FoundationRepository foundationRepository;
    private final IllegalFoundationRepository illegalFoundationRepository;


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
                .collect(Collectors.toList());

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

        // foundation 엔티티 빌드 후 저장.
        //불법 단체에 포함되는 단체면 review_status를 불법 단체 의심으로 변경하고 상세 보기 조회시 해당 상태값과 유사 리스트도 있으면 그것까지 보여주기
        //불법 단체에 포함되지 않는 단체면 임시 비밀번호 메일로 발급하고 기부단체로 저장.


        // 신청이 성공적으로 마무리 되면 repsonseDTO 반환

        return null;

    }

    @Override
    public Page<FoundationListResponseDTO> getFoundationList(String reviewStatus, Pageable pageable) {

        return null;
    }

    @Override
    public FoundationDetailResponseDTO getFoundationDetail(Long foundationNo) {
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));
        return FoundationDetailResponseDTO.from(foundation);
    }
}
