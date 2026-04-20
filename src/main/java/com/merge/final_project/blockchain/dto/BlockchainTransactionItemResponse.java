package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 목록/카드에서 재사용하는 공통 거래 아이템 DTO.
 */
public record BlockchainTransactionItemResponse(
        // 내부 거래 코드(UUID)
        String transactionCode,
        // 체인 tx hash
        String txHash,
        // 블록 번호
        Long blockNum,
        // SUCCESS/FAILED 등 상태
        String status,
        // 프론트 표준 이벤트 코드
        String eventType,
        // 화면 표시용 이벤트 라벨
        String eventTypeLabel,
        // 도메인 단위 금액
        BigDecimal amount,
        // 가스 수수료
        BigDecimal gasFee,
        // 거래 발생 시각
        LocalDateTime sentAt,
        // 송신 지갑 주소
        String fromWalletAddress,
        // 수신 지갑 주소
        String toWalletAddress,
        // 연관 단체명
        String foundationName,
        // 연관 캠페인명
        String campaignName,
        // 이벤트 설명 메모
        String memo,
        // 송신자 타입 라벨
        String fromOwnerTypeLabel,
        // 수신자 타입 라벨
        String toOwnerTypeLabel
) {
}
