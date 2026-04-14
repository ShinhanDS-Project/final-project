package com.merge.final_project.donation.donations;

/**
 * 선우 작성:
 * 결제 이후 토큰 처리 파이프라인 상태 머신.
 *
 * PENDING -> CHARGING -> CHARGED -> DONATING -> DONE
 *             |                      |
 *             v                      v
 *       FAILED_CHARGE         FAILED_DONATION
 */
public enum DonationTokenStatus {
    PENDING,
    CHARGING,
    CHARGED,
    DONATING,
    DONE,
    FAILED_CHARGE,
    FAILED_DONATION
}
