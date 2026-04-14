package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "기부단체 지갑 정보 DTO")
@Getter
@Builder
public class FoundationWalletDTO {

    @Schema(description = "블록체인 지갑 주소 (null이면 지갑 미연동)", example = "0xAbCd1234...5678EfGh")
    private String walletAddress;

    @Schema(description = "지갑 잔액 (GiveN Token 단위)", example = "1234.56")
    private BigDecimal balance;
}
