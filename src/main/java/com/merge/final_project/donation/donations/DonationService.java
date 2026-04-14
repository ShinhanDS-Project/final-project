package com.merge.final_project.donation.donations;

import java.util.List;

public interface DonationService {
    List<Donation> requestDonation(Long userNo);
}
