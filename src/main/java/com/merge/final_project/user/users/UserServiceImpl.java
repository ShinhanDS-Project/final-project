package com.merge.final_project.user.users;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.campaign.settlement.dto.SelectSettlementResponseDTO;
import com.merge.final_project.donation.donations.Donation;
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
import com.merge.final_project.user.users.dto.UserTransactionResponseDTO;
import com.merge.final_project.user.users.dto.UserWalletResponseDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.user.verify.VerificationService;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public String login(UserLoginRequestDTO dto) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 2. 계정 상태 확인
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 3. 로그인 실패 횟수(loginCount)가 5회 이상이면 계정 잠금 처리
        if (user.getLoginCount() != null && user.getLoginCount() >= 5) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 4. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            int currentCount = (user.getLoginCount() == null ? 0 : user.getLoginCount());
            int newCount = currentCount + 1;
            
            // [중요] 독립된 트랜잭션에서 실패 횟수를 업데이트하여 로그인 실패 예외 시에도 커밋되도록 함
            updateLoginCount(user.getEmail(), newCount);
            
            if (newCount >= 5) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 5. 로그인 성공 시 실패 횟수 초기화 및 토큰 생성
        updateLoginCount(user.getEmail(), 0);
        return jwtTokenProvider.createGeneralAccessToken(user.getName(), user.getEmail(), "ROLE_USER", user.getUserNo());
    }

    /**
     * 로그인 실패 횟수를 업데이트합니다.
     * Propagation.REQUIRES_NEW를 사용하여 상위 트랜잭션의 롤백 여부와 상관없이 독립적으로 커밋됩니다.
     */
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void updateLoginCount(String email, int count) {
        User user = userRepository.findByEmailAndLoginType(email, LoginType.LOCAL)
                .orElse(null);
        if (user != null) {
            user.setsLoginCount(count);
            userRepository.saveAndFlush(user);
        }
    }


    @Override
    public EmailResponseDTO findEmail(String phone, String name) {
        // 1. 전화번호에서 하이픈 제거 (사용자 입력 포맷과 DB 저장 포맷 일치시키기)
        String cleanPhone = phone.replaceAll("-", "");

        // 2. 핸드폰과 이름으로 조회하여 존재하지 않는다면 예외 발생
        User user = userRepository.findByPhoneAndName(cleanPhone, name)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 존재한다면 마스킹된 이메일과 로그인 타입 반환
        return new EmailResponseDTO(MaskingUtils.maskEmail(user.getEmail()), user.getLoginType());
    }

    // 1단계: 비밀번호 재설정 요청
    @Override
    public void requestPasswordReset(ChangePasswordRequestDTO dto) {
        // [수정] LoginType.LOCAL을 추가하여 중복 이메일(Google/Local) 중 일반 가입 계정만 정확히 조회함
        User user = userRepository.findByEmailAndNameAndLoginType(dto.getEmail(), dto.getName(), LoginType.LOCAL)
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

       // System.out.println("비밀번호 변경 시도 - 대상 UserNo: " + user.getUserNo() + ", 이메일: " + user.getEmail() + ", LoginType: " + user.getLoginType());

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
        // [추가] 비밀번호 변경 성공 시 로그인 실패 횟수를 초기화하여 잠금을 해제함
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setsLoginCount(0);
        userRepository.saveAndFlush(user);
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
                .birth(user.getBirth())
                .profilePath(user.getProfilePath() != null ? fileService.getFilePath(user.getProfilePath()) : null)
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
        // 1. 캠페인 체크
        Campaign campaign = campaignRepository.findByCampaignNo(campaignNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 2. 정산내역 (가장 확실한 ID 기반 조회)
        List<Settlement> settlements = settlementRepository.findByCampaign_CampaignNo(campaignNo);
        Optional<Settlement> settlementOpt = settlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.COMPLETED)
                .max(java.util.Comparator.comparingLong(Settlement::getSettlementNo));

        SelectSettlementResponseDTO settlementDTO = null;
        if (settlementOpt.isPresent()) {
            Settlement s = settlementOpt.get();
            settlementDTO = SelectSettlementResponseDTO.builder()
                    .foundationNo(s.getFoundation() != null ? s.getFoundation().getFoundationNo() : null)
                    .foundationAmount(s.getFoundationAmount())
                    .settledAt(s.getSettledAt())
                    .beneficiaryAmount(s.getBeneficiaryAmount())
                    .beneficiaryNo(s.getBeneficiary() != null ? s.getBeneficiary().getBeneficiaryNo() : null)
                    .settlementStatus(s.getStatus())
                    .build();
        }

        // 3. 리포트 및 날짜 계산
        Optional<FinalReport> finalReportOpt = finalReportRepository.findByCampaign_no(campaignNo);

        long day = 0;
        boolean isPassed = false;
        String trackingStatus = "FUNDRAISING"; // 기본값

        LocalDate today = LocalDate.now();

        if (campaign.getEndAt() != null && campaign.getUsageEndAt() != null) {
            LocalDate fundraisingEndAt = campaign.getEndAt().toLocalDate(); // 모금 종료일
            LocalDate usageEndAt = campaign.getUsageEndAt().toLocalDate();     // 사업 종료일

            if (!today.isAfter(fundraisingEndAt)) {
                // 1. 모금 중 (모금 종료일까지 남은 기간)
                day = ChronoUnit.DAYS.between(today, fundraisingEndAt);
                isPassed = false;
                trackingStatus = "FUNDRAISING";
            } else if (!today.isAfter(usageEndAt)) {
                // 2. 모금 종료 후 사업 진행 중 (사업 종료일까지 남은 기간)
                day = ChronoUnit.DAYS.between(today, usageEndAt);
                isPassed = false;
                trackingStatus = "IN_PROGRESS";
            } else {
                // 3. 사업 종료 후 (사업 종료일로부터 경과된 기간)
                day = ChronoUnit.DAYS.between(usageEndAt, today);
                isPassed = true;
                trackingStatus = "FINISHED";
            }
        }

        FinalReportMicroTrackingResponseDto.FinalReportData reportData = null;
        if (finalReportOpt.isPresent()) {
            FinalReport fr = finalReportOpt.get();
            reportData = FinalReportMicroTrackingResponseDto.FinalReportData.builder()
                    .title(fr.getTitle())
                    .content(fr.getContent())
                    .build();
        }

        FinalReportMicroTrackingResponseDto finalReportMicroDTO = FinalReportMicroTrackingResponseDto.builder()
                .dayPassed(day)
                .isExist(finalReportOpt.isPresent())
                .isPassed(isPassed)
                .trackingStatus(trackingStatus)
                .reportData(reportData)
                .build();
        return MicroTrackingDTO.builder()
                .campaignNo(campaignNo)
                .userFinalReportDTO(finalReportMicroDTO)
                .userSettlementDTO(settlementDTO)
                .build();
    }

    @Override
    public UserWalletResponseDTO showUserWalletInfo(Long userNo) {

        //1. 지갑 조회해오기(하드코딩한 이유- > 어차피 user 마이페이지에서만 조회함.
        Wallet wallet= walletRepository.findByOwnerNoAndWalletType(userNo, WalletType.USER)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_WALLET_NOT_FOUND));



        return UserWalletResponseDTO.builder()
                .walletNo(wallet.getWalletNo())
                .walletAddress(wallet.getWalletAddress())
                .walletStatus(wallet.getStatus())
                .walletType(wallet.getWalletType())
                .ownerNo(wallet.getOwnerNo())
                .balance(wallet.getBalance())
                .walletHash(wallet.getWalletHash())
                .build();
    }

    @Override
    public List<UserTransactionResponseDTO> showWalletTokenTrans(Long userNo) {

        // 1. 해당 유저의 모든 기부 내역을 가져옵니다. (시작점)
        List<Donation> donationList = donationRepository.findByUserNo(userNo);

        Long transactionNum = donationRepository.countByUserNo(userNo);
        BigDecimal totalAmountWon = donationRepository.sumDonationAmountByUserNo(userNo);

        if (donationList.isEmpty()) {
            return List.of();
        }

        // 2. 각 기부 내역을 DTO로 변환합니다.
        List<Long> campaignNos = donationList.stream()
                .map(Donation::getCampaignNo)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<Long> transactionNos = donationList.stream()
                .map(Donation::getTransactionNo)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        java.util.Map<Long, Campaign> campaignMap = campaignRepository.findAllByCampaignNoIn(campaignNos).stream()
                                .collect(java.util.stream.Collectors.toMap(Campaign::getCampaignNo, c -> c));
                java.util.Map<Long, Transaction> transactionMap = transactionRepository.findAllByTransactionNoIn(transactionNos).stream()
                                .collect(java.util.stream.Collectors.toMap(Transaction::getTransactionNo, t -> t));
        return donationList.stream()
                                .map(donation -> {
                                Campaign campaign = campaignMap.get(donation.getCampaignNo());
                                Transaction transaction = transactionMap.get(donation.getTransactionNo());
            return UserTransactionResponseDTO.builder()
                    .transaction(transaction)
                    .campaignNo(donation.getCampaignNo())
                    .userNo(donation.getUserNo())
                    .title(campaign != null ? campaign.getTitle() : "정보 없음")
                    .approvalStatus(campaign != null ? campaign.getApprovalStatus() : null)
                    .priceamount(donation.getDonationAmount())
                    .total_amount(totalAmountWon)
                    .transactionNum(transactionNum)
                    .build();
        })
                .toList();

    }


}

