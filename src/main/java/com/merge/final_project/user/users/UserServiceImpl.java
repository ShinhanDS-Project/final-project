package com.merge.final_project.user.users;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.campaign.settlement.dto.SelectSettlementResponseDTO;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.global.utils.MaskingUtils;
import com.merge.final_project.global.utils.S3FileService;
import com.merge.final_project.report.finalreport.dto.FinalReportMicroTrackingResponseDto;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import com.merge.final_project.user.users.dto.MicroTrackingDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.user.verify.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository; // 또는 UserMapper (MyBatis 사용 시)
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;
    private final S3FileService fileService;
    private final DonationRepository donationRepository;
    private final SettlementRepository settlementRepository;
    private final CampaignRepository campaignRepository;
    private final FinalReportRepository finalReportRepository;
    @Override
    @Transactional
    public String login(UserLoginRequestDTO dto) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        //2. 계정상태를 확인하기
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new RuntimeException("로그인할 수 없는 계정입니다. 문의바랍니다");
        }
        //3. (부가기능) login_count>=5이면 로그인 불가(비밀번호 재설정 인데, 일단 막아두기 --> 부가기능)
        if (user.getLoginCount() >= 5) {
            throw new RuntimeException("해당 계정의 로그인 횟수가 초과되었습니다. 문의바랍니다.");
        }
        //4.비밀번호 확인하기
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            user.setsLoginCount(user.getLoginCount() + 1);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        //5. 정상 로그인을 위해 jwt 토큰 활용
        user.setsLoginCount(0);
        return jwtTokenProvider.createGeneralAccessToken(user.getName(), user.getEmail(), "ROLE_USER", user.getUserNo());
    }


    @Override
    public EmailResponseDTO findEmail(String phone, String name) {
        //1. 핸드폰과 번호로 현재 존재하지 않는다면 존재하지 않다고 띄우기
        User user = userRepository.findByPhoneAndName(phone, name)
                .orElseThrow(() -> new RuntimeException("가입정보가 없습니다. 가입해주세요"));
        //2. 존재한다면 이메일과 login Type 반환

        return new EmailResponseDTO(MaskingUtils.maskEmail(user.getEmail()), user.getLoginType());
    }

    // 1단계: 비밀번호 재설정 요청
    @Override
    public void requestPasswordReset(ChangePasswordRequestDTO dto) {
        User user = userRepository.findByEmailAndName(dto.getEmail(), dto.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        verificationService.sendPasswordResetCode(user.getEmail());
    }

    // 2단계: 인증코드 확인
    @Override
    public void confirmPasswordCode(String email, String code) {
        User user = userRepository.findByEmailAndLoginType(email, LoginType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        verificationService.verifyCode(user.getEmail(), code);
    }

    // 로그인 후 마이페이지 비밀번호 변경
    @Override
    @Transactional
    public void editPassword(Long userNo, EditPasswordDTO dto) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("새 비밀번호는 필수입니다.");
        }
        //  평문 암호끼리의 비교
        if (!dto.getNewPassword().equals(dto.getNewPassword2())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        // [추가] 검증 완료 후 새 비밀번호를 암호화하여 DB(엔티티)에 반영
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
    }

    // 3단계: 로그인 전 최종 비밀번호 재설정

    @Transactional
    public void resetPassword(ChangeResetPasswordRequestDTO dto) {
        User user = userRepository.findByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!verificationService.isVerifiedEmail(dto.getEmail())) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        if (!dto.getNewPassword().equals(dto.getNewPassword2())) {
            throw new RuntimeException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }
        //새 비밀번호 암호화하여 db에 반영하기
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setsLoginCount(0);
        verificationService.deleteVerification(dto.getEmail());
    }

    @Override
    public MyInfoResponseDTO getMyInfo(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MyInfoResponseDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .nameHash(user.getNameHash())
                .profilePath(user.getProfilePath())
                .build();

    }

    @Transactional
    @Override
    public void updateMyInfo(Long userNo, UpdateMyInfoRequestDTO dto) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 수정
        if (dto.getNameHash() != null && !dto.getNameHash().isBlank()) {
            if (!dto.getNameHash().equals(user.getNameHash())
                    && userRepository.existsByNameHash(dto.getNameHash())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.setNameHash(dto.getNameHash());
        }
        // 프로필 사진 수정
        MultipartFile file = dto.getProfileImage();
        if (file != null && !file.isEmpty()) {
            try {
                if (user.getProfilePath() != null && !user.getProfilePath().isBlank()) {
                    fileService.deleteFile(user.getProfilePath());
                }

                String storedName = fileService.saveFile(file);
                user.setProfilePath(storedName);
            } catch (Exception e) {
                throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", e);
            }
        }
    }

    @Override
    public MicroTrackingDTO showMicroTracking(Long campaignNo) {
        // ---> 1. 정산내용
        // Optional을 활용하여 정산이 없는 경우 예외를 던지지 않고 처리
        Optional<Settlement> settlementOpt = settlementRepository.findByCampaign_CampaignNo(campaignNo).stream()
                .filter(s -> s.getStatus().equals(SettlementStatus.COMPLETED))
                .findFirst();

        SelectSettlementResponseDTO settlementDTO;
        if (settlementOpt.isPresent()) {
            // 정산 완료된 내역이 있을 때
            Settlement settlement = settlementOpt.get();
            settlementDTO = SelectSettlementResponseDTO.builder()
                    .foundationNo(settlement.getFoundation().getFoundationNo())
                    .foundationAmount(settlement.getFoundationAmount())
                    .settledAt(settlement.getSettledAt())
                    .beneficiaryAmount(settlement.getBeneficiaryAmount())
                    .beneficiaryNo(settlement.getBeneficiary().getBeneficiaryNo())
                    .settlementStatus(settlement.getStatus()) // COMPLETED
                    .build();
        } else {
            // 정산 대기 중이거나 정산 내역이 아예 없는 경우 (예외 발생 안 함)
            settlementDTO = SelectSettlementResponseDTO.builder()
                    // 필요한 경우 null 대신 0을 넣거나, PENDING 상태를 반환하도록 세팅
                    .settlementStatus(null) // 혹은 SettlementStatus.PENDING 등 프론트와 약속한 상태
                    .build();
        }



//---------------------------------------------------------------------------------------
        //1. 실제로 존재하는 캠페인인지 확인
        Campaign campaign= campaignRepository.findByCampaignNo(campaignNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        //2. 리포트 여부 확인
        Optional<FinalReport> finalReport= finalReportRepository.findByCampaign_no(campaignNo);
        FinalReportMicroTrackingResponseDto finalReportMicroDTO;

        // 3. 사업 종료일 확인 및 시간 함정(24시간) 피하기 위해 LocalDate 사용
        LocalDate nextDay = campaign.getUsageEndAt().plusDays(1).toLocalDate(); // 종료일 다음 날의 순수 날짜
        LocalDate today = LocalDate.now(); // 오늘의 순수 날짜

        // 순수 날짜끼리 비교하므로 24시간이 지나지 않아도 정확히 일수(D-Day)를 계산
        long day = Math.abs(ChronoUnit.DAYS.between(nextDay, today));
        boolean isPassed = today.isAfter(nextDay); // 오늘이 다음 날을 지났는지 판별

        if(finalReport.isEmpty()) {
            //3. 사업 종료일 확인
            // 캠페인 엔티티에서 사업종료일+1 가져오기
            // 기준은 종료일 다음날 00시 00분 설정이므로
            // 종료일 다음날 00시 00분 설정

          if (isPassed) {
                    // 몇일 지났는지 확인해야함

                    finalReportMicroDTO = FinalReportMicroTrackingResponseDto.builder()
                            .dayPassed(day)
                            .isExist(false) // 리포트 존재 여부 플래그
                            .reportData(null)  // 데이터는 없음
                            .isPassed(true)
                            .build();
          }
          else{
              finalReportMicroDTO = FinalReportMicroTrackingResponseDto.builder()
                      .dayPassed(day)
                      .isExist(false) // 리포트 존재 여부 플래그
                      .reportData(null)  // 데이터는 없음
                      .isPassed(false)
                      .build();
          }
        }
        else {
            FinalReport finalReportNotEmpty = finalReport.get();
            // 3. 모든 조건을 통과하면 리포트 반환
            if (isPassed) {
                finalReportMicroDTO = FinalReportMicroTrackingResponseDto.builder()
                        .dayPassed(day)
                        .isExist(true)
                        .isPassed(true)
                        .reportData(FinalReportMicroTrackingResponseDto.FinalReportData.builder()
                                .title(finalReportNotEmpty.getTitle())
                                .content(finalReportNotEmpty.getContent())
                                .build()).build();
            } else {
                finalReportMicroDTO = FinalReportMicroTrackingResponseDto.builder()
                        .dayPassed(day)
                        .isExist(true)
                        .isPassed(false)
                        .reportData(FinalReportMicroTrackingResponseDto.FinalReportData.builder()
                                .title(finalReportNotEmpty.getTitle())
                                .content(finalReportNotEmpty.getContent())
                                .build()).build();

            }
        }
        return MicroTrackingDTO.builder()
                .UserfinalReportDTO(finalReportMicroDTO)
                .UsersettlementDTO(settlementDTO)
                .build();
    }




}

