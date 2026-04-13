package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoundationRepository extends JpaRepository<Foundation, Long> {

    // 로그인용 이메일 조회
    Optional<Foundation> findByFoundationEmail(String foundationEmail);
    //사업자등록번호로 존재하는지 확인
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    //반려 목록 용.
    Page<Foundation> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);
    //신청 목록 용. approved와 rejected는 제외.
    Page<Foundation> findByReviewStatusNotIn(List<ReviewStatus> reviewStatuses, Pageable pageable);
    //승인 이후 목록 용. 활성화와 비활성화 상태는 필터링으로 조회)
    Page<Foundation> findByReviewStatusAndAccountStatus(ReviewStatus reviewStatus, AccountStatus accountStatus, Pageable pageable);
    //기부단체 상세조회 - PK 값으로
    Optional<Foundation> findByFoundationNo(Long foundationNo);
    /**
     * 단체명 대소문자 무시 단건 조회.
     * 대시보드 검색어 해석 시 단체 지갑 탐색의 시작점으로 사용한다.
     */
    Optional<Foundation> findFirstByFoundationNameIgnoreCase(String foundationName);

    // [가빈] 대시보드용 - 신규 신청 건수 (APPROVED, REJECTED 제외)
    long countByReviewStatusNotIn(List<ReviewStatus> reviewStatuses);
}
