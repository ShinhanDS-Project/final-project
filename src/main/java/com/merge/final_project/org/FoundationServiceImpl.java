package com.merge.final_project.org;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.notification.email.event.FoundationApprovedEvent;
import com.merge.final_project.notification.email.event.FoundationRejectedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationServiceImpl implements FoundationService {

    private final FoundationRepository foundationRepository;
    private final IllegalFoundationRepository illegalFoundationRepository;
    private final FileUtil upload;    //프로필 이미지는 위한 업로드 필드 추가
    private final PasswordEncoder passwordEncoder;  //임시 비밀번호 생성 시 암호화 하여 저장하기 위함
    private final ApplicationEventPublisher eventPublisher; // 언제 이벤트 발행할지 설정하기 위함.
    private final AdminLogService adminLogService;
    private final AdminRepository adminRepository;


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

    //사업자 등록번호로 중복 체크
    @Override
    public boolean existByBusinessRegistrationNumber(String businessRegistrationNumber) {
        return foundationRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber);
    }

    @Transactional
    @Override
    public FoundationApplyResponseDTO apply(FoundationApplyRequestDTO requestDTO, MultipartFile profileImage) {

        if (foundationRepository.existsByBusinessRegistrationNumber(requestDTO.getBusinessRegistrationNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION);
        }

        //불법 단체 체크
        IllegalFoundationResponseDTO checkedFoundation = checkIllegalFoundation(requestDTO);

        //포함 여부에 따라 상태 변경
        ReviewStatus reviewStatus;
        if (checkedFoundation.isExactMatch()) {
            reviewStatus = ReviewStatus.ILLEGAL;
        } else if (!checkedFoundation.getSimilarFoundations().isEmpty()) {
            reviewStatus = ReviewStatus.SIMILAR;
        } else {
            reviewStatus = ReviewStatus.CLEAN;
        }

        //프로필 사진 저장
        String profilePath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profilePath = upload.saveFile(profileImage);
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
                .profilePath(profilePath)
                .contactPhone(requestDTO.getContactPhone())
                .account(requestDTO.getAccount())
                .feeRate(requestDTO.getFeeRate())
                .foundationHash(null)
                .accountStatus(AccountStatus.PRE_REGISTERED)
                .reviewStatus(reviewStatus)
                .rejectReason(checkedFoundation.isExactMatch()
                        ? checkedFoundation.getMatchedFoundation().getReason()
                        : null)
                .campaignWallet1(null)
                .campaignWallet2(null)
                .campaignWallet3(null)
                .bankName(requestDTO.getBankName())
                .foundationType(requestDTO.getFoundationType())
                .build();

        //기부단체 저장할 때 중복 오류 발생하면 에러코드 발생.
        try {
            foundationRepository.save(foundation);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION);
        }

        // 관리자에게 새 가입 신청 SSE 알림 (커밋 후 전송)
        eventPublisher.publishEvent(new ApprovalRequestEvent(
                TargetType.FOUNDATION,
                foundation.getFoundationNo(),
                foundation.getFoundationName() + " 기부단체 가입 신청"));

        return FoundationApplyResponseDTO.builder()
                .foundationEmail(foundation.getFoundationEmail())
                .foundationName(foundation.getFoundationName())
                .representativeName(foundation.getRepresentativeName())
                .build();
    }

    @Override
    public Page<FoundationListResponseDTO> getFoundationApplicationList(Pageable pageable) {
        // 신청 해놓고 승인되지 않은 기부단체들을 볼 수 있는 목록 (승인인지 반려인지를 제외)
        List<ReviewStatus> reviewStatuses = new ArrayList<>();
        reviewStatuses.add(ReviewStatus.APPROVED);
        reviewStatuses.add(ReviewStatus.REJECTED);

        Page<Foundation> foundations = foundationRepository.findByReviewStatusNotIn(reviewStatuses, pageable);
        return foundations.map(FoundationListResponseDTO::from);
    }

    @Override
    public Page<FoundationListResponseDTO> getRejectedFoundationList(Pageable pageable) {
        //반려된 기부단체들 보기
        ReviewStatus reviewStatus = ReviewStatus.REJECTED;
        Page<Foundation> foundations = foundationRepository.findByReviewStatus(reviewStatus, pageable);
        return foundations.map(FoundationListResponseDTO::from);
    }

    @Override
    public Page<FoundationListResponseDTO> getApprovedFoundationList(AccountStatus accountStatus, Pageable pageable) {
        //승인된 기부단체들 보기 (비활성화, 활성화 필터링)
        Page<Foundation> foundations;
        if (accountStatus == null) {
            foundations = foundationRepository.findByReviewStatus(ReviewStatus.APPROVED, pageable);
        } else {
            foundations = foundationRepository.findByReviewStatusAndAccountStatus(ReviewStatus.APPROVED, accountStatus, pageable);
        }
        return foundations.map(FoundationListResponseDTO::from);
    }

    @Override
    public FoundationDetailResponseDTO getFoundationDetail(Long foundationNo) {
        //기부 단체 상세보기 페이지
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));
        return FoundationDetailResponseDTO.from(foundation);
    }

    // 기부단체 승인 시 임시 비밀번호 생성 후 메일 보내는 메서드 비동기로 호출,
    @Transactional
    @Override
    public Long approveFoundation(Long foundationNo) {
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));

        //불법 단체는 승인 불가 하도록 한 번 더 조회 + 반려되는 경우는 비활성화랑 다름. 재가입 불가. 따라서 반려되는 경우도 조회.
        if (foundation.getReviewStatus() == ReviewStatus.ILLEGAL || foundation.getReviewStatus() == ReviewStatus.REJECTED) {
            throw new BusinessException(ErrorCode.CANNOT_APPROVE_ILLEGAL_FOUNDATION);
        }

        foundation.approved();

        //임시 비밀번호 생성한 후 해당 내용 DB에 저장.
        String sendTempPassword = String.valueOf(UUID.randomUUID());
        String tempPassword = passwordEncoder.encode(sendTempPassword);

        foundation.updatePassword(tempPassword);

        //트랜잭션 커밋 성공한 이후에만 메일 발송 -> 이벤트 기반으로 구현.
        eventPublisher.publishEvent(new FoundationApprovedEvent(foundationNo,foundation.getFoundationEmail(), foundation.getFoundationName(), sendTempPassword));

        //관리자 활동 로그 기록부분
        //인가 토큰에서 adminId 추출해와서 그걸로 관리자 정보 찾고 로그 기옥
        String adminId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        //활동 상세 내역 적어서 로그 기록 남길 것.
        String description = foundation.getFoundationName() + " 기부단체 승인 후 임시 비밀번호 메일 전송";
        adminLogService.log(ActionType.APPROVE, TargetType.FOUNDATION, foundationNo, description, admin);

        return foundation.getFoundationNo();
    }

    //반려 시 반려 사유를 담아 메일 보내는 메서드 비동기로 호출
    @Override
    @Transactional
    public Long rejectFoundationForIllegal(Long foundationNo) {
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));

        foundation.reject(foundation.getRejectReason());

        //메일 발송
        eventPublisher.publishEvent(new FoundationRejectedEvent(foundationNo, foundation.getFoundationEmail(), foundation.getFoundationName(), foundation.getRejectReason()));

        //관리자 활동 로그 기록부분
        //인가 토큰에서 adminId 추출해와서 그걸로 관리자 정보 찾고 로그 기옥
        String adminId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        //활동 상세 내역 적어서 로그 기록 남길 것.
        String description = foundation.getFoundationName() + " 기부단체 반려 후 메일 전송. 반려사유: " + foundation.getRejectReason();
        adminLogService.log(ActionType.REJECT, TargetType.FOUNDATION, foundationNo, description, admin);

        return foundation.getFoundationNo();
    }
}
