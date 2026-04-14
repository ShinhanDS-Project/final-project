package com.merge.final_project.blockchain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 토큰 금액을 온체인 단위와 사람이 읽는 단위 간 변환해주는 클래스
 *
 * ex) ERC-20 토큰은 보통 18자리 소수점을 사용
 *     1 토큰 = 10^18 (온체인 단위)
 */
@Component
public class TokenAmountConverter {
    // 토큰의 소수점 자리수 (기본값: 18)
    private final int tokenDecimals;

    // 설정값에서 토큰 소수점 주입 (없으면 18 사용)
    public TokenAmountConverter(@Value("${blockchain.token.decimals:18}") int tokenDecimals) {
        this.tokenDecimals = tokenDecimals;
    }
    // Long 금액을 온체인 단위로 변환 (amount * 10^decimals)
    public BigInteger toOnChainAmount(Long amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        return BigInteger.valueOf(amount).multiply(BigInteger.TEN.pow(tokenDecimals));
    }
    // BigDecimal 금액을 온체인 단위로 변환 (소수 포함 처리)
    public BigInteger toOnChainAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        return amount.movePointRight(tokenDecimals).toBigIntegerExact(); // 소수 남으면 예외 발생
    }
    // 온체인 금액을 사람이 읽는 단위로 변환 (amount / 10^decimals)
    public BigDecimal fromOnChainAmount(BigInteger amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(amount).movePointLeft(tokenDecimals);
    }
}
