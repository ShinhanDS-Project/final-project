package com.merge.final_project.blockchain.security;

/**
 * 지갑 생성 시 반환되는 최소 자격정보(주소 + 암호화 개인키).
 */
public record WalletCredentials(String address, String encryptedPrivateKey) {
}
