package com.merge.final_project.blockchain.security;

/**
 * 지갑 생성 시 반환되는 최소 정보 묶음.
 * address는 공개 저장값, encryptedPrivateKey는 DB 키 테이블 저장값으로 사용한다.
 */
public record WalletCredentials(
        // 0x prefix를 포함한 지갑 주소
        String address,
        // AES-GCM으로 암호화된 private key payload
        String encryptedPrivateKey
) {
}
