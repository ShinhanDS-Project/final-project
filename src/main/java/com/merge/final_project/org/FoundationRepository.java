package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // [가빈] 추가
import org.springframework.data.repository.query.Param; // [가빈] 추가

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

    //기부단체 상세조회 - PK 값으로
    Optional<Foundation> findByFoundationNo(Long foundationNo);
    /**
     * 단체명 대소문자 무시 단건 조회.
     * 대시보드 검색어 해석 시 단체 지갑 탐색의 시작점으로 사용한다.
     */
    Optional<Foundation> findFirstByFoundationNameIgnoreCase(String foundationName);

    // [가빈] 대시보드용 - 신규 신청 건수 (APPROVED, REJECTED 제외)
    long countByReviewStatusNotIn(List<ReviewStatus> reviewStatuses);

    // [가빈] 관리자 승인 단체 목록 — accountStatus 필터 + 키워드 검색 (단체명, 대표자명)
    // keyword null → "" 변환 후 호출 (IS NULL 대신 = '' 사용: PostgreSQL lower(bytea) 오류 방지)
    @Query("SELECT f FROM Foundation f WHERE f.reviewStatus = 'APPROVED' AND (:accountStatus IS NULL OR f.accountStatus = :accountStatus) AND (:keyword = '' OR LOWER(f.foundationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.representativeName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Foundation> findApprovedWithFilter(@Param("accountStatus") AccountStatus accountStatus, @Param("keyword") String keyword, Pageable pageable);

// [가빈] 관리자 신청 목록 — accountStatus = PRE_REGISTERED + reviewStatus 필터 + 키워드 검색 (단체명, 대표자명)
    // keyword null → "" 변환 후 호출 (IS NULL 대신 = '' 사용: PostgreSQL lower(bytea) 오류 방지)
    @Query("SELECT f FROM Foundation f WHERE f.accountStatus = 'PRE_REGISTERED' AND (:reviewStatus IS NULL OR f.reviewStatus = :reviewStatus) AND (:keyword = '' OR LOWER(f.foundationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.representativeName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Foundation> findApplicationsWithFilter(@Param("reviewStatus") ReviewStatus reviewStatus, @Param("keyword") String keyword, Pageable pageable);

    // [가빈] 관리자 반려 목록 — 키워드 검색 (단체명, 대표자명)
    // keyword null → "" 변환 후 호출 (IS NULL 대신 = '' 사용: PostgreSQL lower(bytea) 오류 방지)
    @Query("SELECT f FROM Foundation f WHERE f.reviewStatus = :reviewStatus AND (:keyword = '' OR LOWER(f.foundationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.representativeName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Foundation> findRejectedWithFilter(@Param("reviewStatus") ReviewStatus reviewStatus, @Param("keyword") String keyword, Pageable pageable);
}
