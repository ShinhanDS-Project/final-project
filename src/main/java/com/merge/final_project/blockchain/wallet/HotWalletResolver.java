package com.merge.final_project.blockchain.wallet;

import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotWalletResolver {

    private final WalletLookupRepository walletLookupRepository;

    public Wallet resolve(String configuredHotWalletAddress) {
        if (configuredHotWalletAddress == null || configuredHotWalletAddress.isBlank()) {
            throw new IllegalStateException("configured hot wallet address is empty");
        }
        Wallet hotWallet = walletLookupRepository.findByWalletAddressIgnoreCase(configuredHotWalletAddress)
                .orElseThrow(() -> new IllegalStateException(
                        "HOT wallet not found by configured address: " + configuredHotWalletAddress
                ));
        if (hotWallet.getWalletType() != WalletType.HOT) {
            throw new IllegalStateException(
                    "configured hot wallet address is not HOT type: " + configuredHotWalletAddress
            );
        }
        return hotWallet;
    }
}
