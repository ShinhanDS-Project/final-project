package com.merge.final_project.donation.donations;

import com.merge.final_project.user.users.dto.support.MyDonationResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    boolean existsByPaymentNo(Long paymentNo);
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
   // List<Donation> findByUserNo(Long userNo);

    @Query("""
        SELECT new com.merge.final_project.user.users.dto.support.MyDonationResponseDTO(
            d.donationNo,
            d.campaignNo,
            d.transactionNo,
            c.title,
            c.imagePath,
            d.donatedAt,
            d.donationAmount,
            c.campaignStatus,
            d.tokenStatus
        )
        FROM Donation d
        JOIN Campaign c ON d.campaignNo = c.campaignNo
        WHERE d.userNo = :userNo
        ORDER BY d.donatedAt DESC
    """)
    List<MyDonationResponseDTO> findMyDonationHistory(@Param("userNo") Long userNo);


}
