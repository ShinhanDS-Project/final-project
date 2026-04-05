package com.merge.final_project.recipient.beneficiary.service;

import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BeneficiaryService implements UserDetailsService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(BeneficiarySignupRequestDTO dto){
        // 1. 이메일 중복 검사
        if(beneficiaryRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("이미 사용 중인 이메일입니다");
        }
        // 2. 엔티티 생성 및 저장
        Beneficiary beneficiary = Beneficiary.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .account(dto.getAccount())
                .entry_code(generatedEntryCode())
                .beneficiaryType(dto.getBeneficiaryType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .beneficiaryHash("init_hash")
                .build();
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return saved.getBeneficiaryNo();
    }

    //일단 랜덤 아무거나 넣을게요 참여코드
    private int generatedEntryCode(){
        Random random = new Random();
        return 100000 + random.nextInt(900000);
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

        beneficiaryRepository.save(beneficiary);
    }
}
