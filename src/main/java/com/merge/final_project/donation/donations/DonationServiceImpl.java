package com.merge.final_project.donation.donations;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {


    private final DonationRepository donationRepository;

    @Override
    public List<Donation> requestDonation(Long userNo) {
        //userNo 기준으로 기부내역 조회

        List<Donation> donation= donationRepository.findByUserNo(userNo);
        if(donation.isEmpty()){
            throw new BusinessException(ErrorCode.DONATION_INVALID);
        }

        return donation;
    }
}
