package com.merge.final_project.recipient.beneficiary.service;

import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

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


}
