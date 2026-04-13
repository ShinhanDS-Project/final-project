package com.merge.final_project.recipient.beneficiary.service;

import com.merge.final_project.auth.useraccount.SignupWalletHookService;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryInfoResponseDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryUpdateRequestDTO;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryService implements UserDetailsService {
    private final CampaignRepository campaignRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupWalletHookService signupWalletHookService;


    @Transactional
    public Long signup(BeneficiarySignupRequestDTO dto){
        log.info("--- 회원가입 시작: {} ---", dto.getEmail());
        
        if(beneficiaryRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("이미 사용 중인 이메일입니다");
        }

        try {
            Beneficiary beneficiary = Beneficiary.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .name(dto.getName())
                    .phone(dto.getPhone())
                    .account(dto.getAccount())
                    .entryCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .beneficiaryType(dto.getBeneficiaryType())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .beneficiaryHash("init_hash")
                    .build();

            Beneficiary saved = beneficiaryRepository.save(beneficiary);
            log.info("1. 수혜자 정보 저장 완료 (ID: {})", saved.getBeneficiaryNo());

            // 💡 지갑 생성 시도
//            try {
//                log.info("2. 지갑 생성 훅 호출 시작...");
//                signupWalletHookService.onBeneficiarySignupCompleted(saved.getBeneficiaryNo());
//                log.info("3. 지갑 생성 및 바인딩 완료");
//            } catch (Exception walletEx) {
//                log.error("❌ 지갑 생성 중 심각한 오류 발생: {}", walletEx.getMessage());
//                walletEx.printStackTrace(); // 💡 상세 스택트레이스 출력
//                throw new RuntimeException("지갑 생성 오류: " + walletEx.getMessage());
//            }

            return saved.getBeneficiaryNo();
            
        } catch (Exception e) {
            log.error("❌ 회원가입 최종 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    private String generatedEntryCode(){
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 이메일로 수혜자 찾기
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 수혜자 이메일입니다: " + email));

        // 시큐리티가 이해할 수 있는 유저 정보로 변환
        return User.builder()
                .username(beneficiary.getEmail())
                .password(beneficiary.getPassword()) // 암호화된 비번이어야 함!
                .roles("BENEFICIARY") // 권한 부여
                .build();
    }

    public void signup(BeneficiarySigninRequestDTO dto) {
        // 사용자가 입력한 생비번을 암호화해서 저장해야 로그인이 됩니다.
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        Beneficiary beneficiary = Beneficiary.builder()
                .email(dto.getEmail())
                .password(encodedPassword) // 암호화된 비번 세팅!
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        // 테스트/임시 가입 경로에서도 동일한 지갑 생성 훅을 적용한다.
        signupWalletHookService.onBeneficiarySignupCompleted(saved.getBeneficiaryNo());
    }
    public List<Campaign> getMyCampaigns(String email) {
        // 1. 이메일로 수혜자 번호 찾기
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 없음"));

        // 2. 그 번호로 등록된 캠페인들 가져오기
        return campaignRepository.findByBeneficiaryNo(beneficiary.getBeneficiaryNo());
    }

    /**
     * 이메일로 수혜자 엔티티 조회 (내부 로직용)
     */
    public Beneficiary getBeneficiaryByEmail(String email) {
        return beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다: " + email));
    }

    /**
     * 본인 상세 정보 조회 (조회용)
     */
    public BeneficiaryInfoResponseDTO getMyDetailInfo(String email) {
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));
        return new BeneficiaryInfoResponseDTO(beneficiary);
    }

    /**
     * 본인 정보 조회 (수정 폼용)
     */
    public BeneficiaryUpdateRequestDTO getMyInfo(String email) {
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        BeneficiaryUpdateRequestDTO dto = new BeneficiaryUpdateRequestDTO();
        dto.setName(beneficiary.getName());
        dto.setPhone(beneficiary.getPhone());
        dto.setAccount(beneficiary.getAccount());
        dto.setBeneficiaryType(beneficiary.getBeneficiaryType());

        return dto;
    }

    /**
     * 본인 정보 수정 로직
     */
    @Transactional
    public void updateMyInfo(String email, BeneficiaryUpdateRequestDTO dto) {
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        // 1. 기본 인적 사항 업데이트
        beneficiary.updateInfo(dto.getName(), dto.getPhone(), dto.getAccount(), dto.getBeneficiaryType());

        // 2. 비밀번호 변경 요청 시 처리
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            beneficiary.updatePassword(passwordEncoder.encode(dto.getPassword()));
        }
    }

    public Long getBeneficiaryNoByEntryCode(String entryCode){
        Beneficiary beneficiary = beneficiaryRepository.findByEntryCode(entryCode)
                     .orElseThrow(() -> new RuntimeException("해당 엔트리 코드를 가진 수혜자를 찾을 수 없습니다: " + entryCode));

        return beneficiary.getBeneficiaryNo();
    }

}
