package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoundationRepository extends JpaRepository<Foundation, Long> {
    //사업자등록번호로 존재하는지 확인
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    //Approved된 전체, Approved 안 된 전체
    Page<Foundation> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);
    Page<Foundation> findByReviewStatusNot(ReviewStatus reviewStatus, Pageable pageable);


}
