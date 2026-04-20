package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 상세 페이지 응답 DTO.
 * 목록 DTO보다 확장된 지갑 상세(from/to)를 포함한다.
 */
public record BlockchainTransactionDetailResponse(
        // 내부 거래 코드(UUID)
        String transactionCode,
        // 체인 tx hash
        String txHash,
        // 블록 번호
        Long blockNum,
        // 상태
        String status,
        // 이벤트 코드
        String eventType,
        // 이벤트 라벨
        String eventTypeLabel,
        // 금액
        BigDecimal amount,
        // 발생 시각
        LocalDateTime sentAt,
        // 가스 수수료
        BigDecimal gasFee,
        // 단체명
        String foundationName,
        // 캠페인명
        String campaignName,
        // 메모
        String memo,
        // 송신자 타입 라벨
        String fromOwnerTypeLabel,
        // 수신자 타입 라벨
        String toOwnerTypeLabel,
        // 송신 지갑 요약
        BlockchainWalletSummaryResponse fromWallet,
        // 수신 지갑 요약
        BlockchainWalletSummaryResponse toWallet
) {
}
