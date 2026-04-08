package com.merge.final_project.blockchain.security;

public record WalletCredentials(
        String address,
        String encryptedPrivateKey
) {
}
