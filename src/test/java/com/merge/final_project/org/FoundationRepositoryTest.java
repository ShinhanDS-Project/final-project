package com.merge.final_project.org;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FoundationRepositoryTest {

    @Autowired
    private FoundationRepository foundationRepository;

    private Foundation foundation(String brn, String name, ReviewStatus reviewStatus) {
        return Foundation.builder()
                .foundationEmail(name + "@test.com")
                .foundationName(name)
                .businessRegistrationNumber(brn)
                .representativeName("대표자")
                .description("설명")
                .contactPhone("010-1234-5678")
                .account("123-456-789")
                .feeRate(BigDecimal.valueOf(0.05))
                .bankName("국민은행")
                .foundationType(FoundationType.COMPANY)
                .accountStatus(reviewStatus == ReviewStatus.APPROVED ? AccountStatus.ACTIVE : AccountStatus.PRE_REGISTERED)
                .reviewStatus(reviewStatus)
                .build();
    }

    @Test
    @DisplayName("중복 가입 방지를 위해 존재하는 사업자등록번호 조회 시 true를 반환한다")
    void 사업자등록번호_존재_true() {
        foundationRepository.save(foundation("123-45-67890", "테스트단체A", ReviewStatus.CLEAN));

        boolean result = foundationRepository.existsByBusinessRegistrationNumber("123-45-67890");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사업자등록번호 조회 시 false를 반환한다")
    void 사업자등록번호_미존재_false() {
        boolean result = foundationRepository.existsByBusinessRegistrationNumber("999-99-99999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("APPROVED 상태인 단체만 페이징 조회된다")
    void 승인된_단체_목록_조회() {
        foundationRepository.save(foundation("111-11-11111", "승인단체", ReviewStatus.APPROVED));
        foundationRepository.save(foundation("222-22-22222", "검토중단체", ReviewStatus.CLEAN));
        foundationRepository.save(foundation("333-33-33333", "유사단체", ReviewStatus.SIMILAR));

        Page<Foundation> result = foundationRepository.findByReviewStatus(ReviewStatus.APPROVED, PageRequest.of(0, 10));

        // 실제 DB 데이터가 섞여도 쿼리 필터 자체를 검증
        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(f -> f.getReviewStatus() == ReviewStatus.APPROVED);
    }

    @Test
    @DisplayName("APPROVED, REJECTED를 제외한 단체(CLEAN, SIMILAR, ILLEGAL)만 조회된다")
    void 승인전_단체_목록_조회() {
        foundationRepository.save(foundation("111-11-11111", "승인단체", ReviewStatus.APPROVED));
        foundationRepository.save(foundation("222-22-22222", "반려단체", ReviewStatus.REJECTED));
        foundationRepository.save(foundation("333-33-33333", "검토중단체", ReviewStatus.CLEAN));
        foundationRepository.save(foundation("444-44-44444", "유사단체", ReviewStatus.SIMILAR));
        foundationRepository.save(foundation("555-55-55555", "불법단체", ReviewStatus.ILLEGAL));

        Page<Foundation> result = foundationRepository.findByReviewStatusNotIn(
                List.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED), PageRequest.of(0, 10));

        // 실제 DB 데이터가 섞여도 쿼리 필터 자체를 검증
        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(f -> f.getReviewStatus() != ReviewStatus.APPROVED
                            && f.getReviewStatus() != ReviewStatus.REJECTED);
    }
}
