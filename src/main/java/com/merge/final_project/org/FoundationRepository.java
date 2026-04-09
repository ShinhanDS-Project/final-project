package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundationRepository extends JpaRepository<Foundation, Long> {
    //사업자등록번호로 존재하는지 확인
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    //반려 목록 용.
    Page<Foundation> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);
    //신청 목록 용. approved와 rejected는 제외.
    Page<Foundation> findByReviewStatusNotIn(List<ReviewStatus> reviewStatuses, Pageable pageable);
    //승인 이후 목록 용. 활성화와 비활성화 상태는 필터링으로 조회)
    Page<Foundation> findByReviewStatusAndAccountStatus(ReviewStatus reviewStatus, AccountStatus accountStatus, Pageable pageable);
}
