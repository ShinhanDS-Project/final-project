package com.merge.final_project.blockchain.dto;

/**
 * 검색어 해석 결과 DTO.
 * type 값으로 화면 라우팅 대상을 결정한다.
 */
public record BlockchainSearchResolveResponse(
        // transaction | wallet | not_found
        String type,
        // type에 대응하는 실제 값(txHash 또는 walletAddress)
        String value
) {
}
