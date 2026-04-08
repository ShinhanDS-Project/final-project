package com.merge.final_project.blockchain;

import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.db.entity.Campaign;
import com.merge.final_project.db.entity.KeyEntity;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.repository.CampaignRepository;
import com.merge.final_project.db.repository.KeyEntityRepository;
import com.merge.final_project.db.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SpringBootTest
class WalletKeyConsistencyTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private KeyEntityRepository keyEntityRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private WalletCryptoService walletCryptoService;

    @Test
    void shouldMatchWalletAddressAndPrivateKeyForCriticalWallets() {
        List<Wallet> targets = new ArrayList<>();

        walletRepository.findByWalletTypeAndOwnerNo("SERVER", "HOT").ifPresent(targets::add);
        walletRepository.findByWalletTypeAndOwnerNo("USER", "91001").ifPresent(targets::add);

        Optional<Campaign> campaignOptional = campaignRepository.findById(91001);
        if (campaignOptional.isPresent()) {
            Campaign campaign = campaignOptional.get();
            if (campaign.getWalletNo() != null && !campaign.getWalletNo().isBlank()) {
                long walletNo = Long.parseLong(campaign.getWalletNo().trim());
                walletRepository.findByIdWalletNo(walletNo).ifPresent(targets::add);
            }
        }

        Assertions.assertFalse(targets.isEmpty(), "No target wallets found for consistency check");

        List<String> mismatches = new ArrayList<>();
        for (Wallet wallet : targets) {
            Long walletNo = wallet.getId() == null ? null : wallet.getId().getWalletNo();
            Long keyNo = wallet.getId() == null ? null : wallet.getId().getKeyNo();
            if (keyNo == null) {
                mismatches.add("walletNo=" + walletNo + " has null keyNo");
                continue;
            }

            KeyEntity keyEntity = keyEntityRepository.findById(keyNo)
                    .orElseThrow(() -> new IllegalStateException("key row not found. keyNo=" + keyNo));

            String privateKey = resolvePrivateKey(keyEntity);
            String derivedAddress = Credentials.create(sanitizeHexKey(privateKey)).getAddress();
            String dbAddress = wallet.getWalletAddress();

            if (dbAddress == null || dbAddress.isBlank()) {
                mismatches.add("walletNo=" + walletNo + " has empty wallet_address");
                continue;
            }

            String normalizedDbAddress = dbAddress.toLowerCase(Locale.ROOT);
            if (!normalizedDbAddress.equals(derivedAddress)) {
                mismatches.add("walletNo=" + walletNo
                        + ", keyNo=" + keyNo
                        + ", dbAddress=" + dbAddress
                        + ", derivedAddress=" + derivedAddress);
            }
        }

        Assertions.assertTrue(mismatches.isEmpty(),
                "Wallet/key mismatch found:\n" + String.join("\n", mismatches));
    }

    private String resolvePrivateKey(KeyEntity keyEntity) {
        String stored = keyEntity.getPrivateKey();
        if (stored == null || stored.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + keyEntity.getKeyNo());
        }
        try {
            return walletCryptoService.decryptPrivateKey(stored);
        } catch (RuntimeException e) {
            return stored;
        }
    }

    private String sanitizeHexKey(String privateKeyHex) {
        String value = privateKeyHex == null ? "" : privateKeyHex.trim();
        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("private key is empty");
        }
        return value;
    }
}
