package com.merge.final_project.donation.donations;

import com.merge.final_project.user.users.dto.support.MyDonationResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    boolean existsByPaymentNo(Long paymentNo);
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
   List<Donation> findByUserNo(Long userNo);



}
