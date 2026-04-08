package com.merge.final_project.blockchain.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class WalletCryptoService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int PRIVATE_KEY_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final byte[] encryptionKey;

    /**
     * 로컬 개발 단계용 키 생성/암복호화 헬퍼.
     * KMS/HSM 연동 전 임시 구현이다.
     */
    public WalletCryptoService(@Value("${wallet.crypto.secret:local-wallet-secret-change-me}") String secret) {
        this.encryptionKey = sha256(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 지갑 자격정보(주소/암호화 개인키)를 생성한다.
     * 주의: 현재 주소 생성은 ECDSA 기반 실제 주소 생성이 아닌 임시 방식이다.
     */
    public WalletCredentials createWalletCredentials() {
        byte[] privateKey = new byte[PRIVATE_KEY_BYTES];
        secureRandom.nextBytes(privateKey);
        String address = createPseudoAddress(privateKey);
        String encryptedPrivateKey = encryptToBase64(toHex(privateKey));
        return new WalletCredentials(address, encryptedPrivateKey);
    }

    /**
     * 서명을 위해 암호화된 개인키를 복호화한다.
     */
    public String decryptPrivateKey(String encryptedPrivateKey) {
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedPrivateKey);
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            byte[] cipherText = new byte[payload.length - GCM_NONCE_LENGTH];
            System.arraycopy(payload, 0, nonce, 0, GCM_NONCE_LENGTH);
            System.arraycopy(payload, GCM_NONCE_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcm);

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("wallet private key decryption failed", e);
        }
    }

    private String createPseudoAddress(byte[] privateKey) {
        byte[] digest = sha256(privateKey);
        String hex = toHex(digest);
        return "0x" + hex.substring(hex.length() - 40);
    }

    private String encryptToBase64(String plainText) {
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcm);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[nonce.length + cipherText.length];
            System.arraycopy(nonce, 0, payload, 0, nonce.length);
            System.arraycopy(cipherText, 0, payload, nonce.length, cipherText.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("wallet private key encryption failed", e);
        }
    }

    private byte[] sha256(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("sha-256 hash failed", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
