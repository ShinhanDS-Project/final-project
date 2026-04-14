package com.merge.final_project.blockchain.security;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

@Component
public class WalletPrivateKeyResolver {

    private final KeyRepository keyRepository;
    private final WalletCryptoService walletCryptoService;
    private final String hotWalletPrivateKey;

    public WalletPrivateKeyResolver(
            KeyRepository keyRepository,
            WalletCryptoService walletCryptoService,
            @Value("${blockchain.wallet.hot-private-key:${BLOCKCHAIN_WALLET_HOT_PRIVATE_KEY:}}") String hotWalletPrivateKey
    ) {
        this.keyRepository = keyRepository;
        this.walletCryptoService = walletCryptoService;
        this.hotWalletPrivateKey = hotWalletPrivateKey;
    }

    public String resolveForWallet(Wallet wallet) {
        // 모든 호출자가 동일 포맷(0x 제거된 hex)으로 개인키를 받도록 정규화한다.
        if (wallet == null) {
            throw new IllegalStateException("wallet is null");
        }

        String decryptedPrivateKey = wallet.getWalletType() == WalletType.HOT
                ? resolveHotWalletPrivateKey(wallet)
                : resolveDbWalletPrivateKey(wallet);

        return stripHexPrefix(decryptedPrivateKey);
    }

    private String resolveDbWalletPrivateKey(Wallet wallet) {
        // DB 지갑은 key 테이블의 암호문 payload를 기준으로 복호화한다.
        if (wallet.getKey() == null || wallet.getKey().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletNo=" + wallet.getWalletNo());
        }

        Key key = keyRepository.findById(wallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getKey().getKeyNo()));

        String storedPrivateKey = key.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + key.getKeyNo());
        }
        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            throw new IllegalStateException("wallet private key decryption failed. keyNo=" + key.getKeyNo(), e);
        }
    }

    private String resolveHotWalletPrivateKey(Wallet hotWallet) {
        // HOT 지갑 키는 env에서 가져오며, HOT 지갑 주소와 일치하는지 검증한다.
        if (hotWalletPrivateKey == null || hotWalletPrivateKey.isBlank()) {
            throw new IllegalStateException("configured hot wallet private key is empty");
        }

        try {
            String decryptedPrivateKey = walletCryptoService.decryptPrivateKey(hotWalletPrivateKey);
            Credentials credentials = Credentials.create(stripHexPrefix(decryptedPrivateKey));
            if (!credentials.getAddress().equalsIgnoreCase(hotWallet.getWalletAddress())) {
                throw new IllegalStateException(
                        "configured hot wallet private key does not match configured hot wallet address. address="
                                + hotWallet.getWalletAddress()
                );
            }
            return decryptedPrivateKey;
        } catch (RuntimeException e) {
            throw new IllegalStateException("configured hot wallet private key is invalid", e);
        }
    }

    private String stripHexPrefix(String value) {
        return value != null && value.startsWith("0x") ? value.substring(2) : value;
    }
}
