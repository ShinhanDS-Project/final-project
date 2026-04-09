package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoundationRepository extends JpaRepository<Foundation, Long> {
    /**
     * 사업자등록번호 중복 여부 확인.
     */
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    /**
     * 심사 상태별 단체 목록 조회.
     */
    Page<Foundation> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 특정 심사 상태 집합을 제외한 단체 목록 조회.
     */
    Page<Foundation> findByReviewStatusNotIn(List<ReviewStatus> reviewStatuses, Pageable pageable);

    /**
     * 심사 상태 + 계정 상태 조합 조회.
     */
    Page<Foundation> findByReviewStatusAndAccountStatus(ReviewStatus reviewStatus, AccountStatus accountStatus, Pageable pageable);

    /**
     * 단체명 대소문자 무시 단건 조회.
     * 대시보드 검색어 해석 시 단체 지갑 탐색의 시작점으로 사용한다.
     */
    Optional<Foundation> findFirstByFoundationNameIgnoreCase(String foundationName);
}
