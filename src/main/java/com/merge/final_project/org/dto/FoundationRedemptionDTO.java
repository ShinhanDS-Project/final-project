package com.merge.final_project.org.dto;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.entity.Redemption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "기부단체 환금(현금화) 내역 DTO")
@Getter
@Builder
public class FoundationRedemptionDTO {

    @Schema(description = "환금 신청 번호", example = "1")
    private Long redemptionNo;

    @Schema(description = "환금 신청 금액 (GiveN Token 단위)", example = "1000")
    private Long amount;

    @Schema(description = "환금 상태 (PENDING, PROCESSING, COMPLETED, FAILED)", example = "COMPLETED")
    private RedemptionStatus status;

    @Schema(description = "환금 신청 일시", example = "2024-04-05T10:00:00")
    private LocalDateTime requestedAt;

    @Schema(description = "처리 시작 일시 (null이면 미처리)", example = "2024-04-05T10:30:00")
    private LocalDateTime processedAt;

    @Schema(description = "현금 지급 완료 일시 (null이면 미지급)", example = "2024-04-05T11:00:00")
    private LocalDateTime cashPaidAt;

    @Schema(description = "실패 사유 (실패한 경우에만 값 존재)", example = "계좌 정보 불일치")
    private String failureReason;

    public static FoundationRedemptionDTO from(Redemption redemption) {
        return FoundationRedemptionDTO.builder()
                .redemptionNo(redemption.getRedemptionNo())
                .amount(redemption.getAmount())
                .status(redemption.getStatus())
                .requestedAt(redemption.getRequestedAt())
                .processedAt(redemption.getProcessedAt())
                .cashPaidAt(redemption.getCashPaidAt())
                .failureReason(redemption.getFailureReason())
                .build();
    }
}
