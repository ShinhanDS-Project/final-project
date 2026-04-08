package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.db.entity.TokenTransaction;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.repository.TokenTransactionRepository;
import com.merge.final_project.db.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class EventService {

    private final TokenTransactionRepository tokenTransactionRepository;
    private final WalletRepository walletRepository;

    /**
     * receipt 파싱 결과를 기존 token_transaction row에 반영한다.
     * 저장 순서는 기본 저장 -> 이벤트 파싱값 보정이다.
     */
    @Transactional
    public TokenTransaction applyParsedEvent(TokenTransaction tx, TransferResult result, int tokenDecimals) {
        if (tx == null || result == null) {
            return tx;
        }

        if (result.txHash() != null) {
            tx.setTxHash(result.txHash());
        }
        if (result.blockNumber() != null) {
            tx.setBlockNum(toSafeInt(result.blockNumber()));
        }
        if (result.status() != null) {
            tx.setStatus(result.status());
        }
        if (result.eventType() != null) {
            tx.setEventType(result.eventType());
        }
        if (result.fromAddress() != null) {
            walletRepository.findByWalletAddressIgnoreCase(result.fromAddress())
                    .ifPresent(wallet -> tx.setFromWalletNo(toWalletNo(wallet)));
        }
        if (result.toAddress() != null) {
            walletRepository.findByWalletAddressIgnoreCase(result.toAddress())
                    .ifPresent(wallet -> tx.setToWalletNo(toWalletNo(wallet)));
        }
        if (result.onChainAmount() != null) {
            tx.setAmount(toDisplayAmount(result.onChainAmount(), tokenDecimals));
        }

        return tokenTransactionRepository.save(tx);
    }

    /**
     * 체인 최소 단위를 DB 표시 단위(토큰 단위)로 변환한다.
     */
    private Integer toDisplayAmount(BigInteger onChainAmount, int tokenDecimals) {
        BigInteger divisor = BigInteger.TEN.pow(tokenDecimals);
        BigInteger display = onChainAmount.divide(divisor);
        if (display.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }
        if (display.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            return Integer.MIN_VALUE;
        }
        return display.intValue();
    }

    private Integer toSafeInt(Long value) {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }

    private Integer toWalletNo(Wallet wallet) {
        if (wallet.getId() == null) {
            return null;
        }
        return toSafeInt(wallet.getId().getWalletNo());
    }
}
