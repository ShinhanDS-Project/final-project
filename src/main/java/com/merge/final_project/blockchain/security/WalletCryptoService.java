package com.merge.final_project.blockchain.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

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

    public WalletCryptoService(@Value("${wallet.crypto.secret:local-wallet-secret-change-me}") String secret) {
        this.encryptionKey = sha256(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 랜덤 private key를 생성하고 주소를 파생한 뒤, 암호화된 키 payload를 반환한다.
     * 저장 포맷은 Base64( nonce(12바이트) + AES-GCM ciphertext ) 구조다.
     */
    public WalletCredentials createWalletCredentials() {
        try {
            byte[] privateKeyBytes = new byte[PRIVATE_KEY_BYTES];
            secureRandom.nextBytes(privateKeyBytes);
            String privateKeyHex = toHex(privateKeyBytes);

            ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);
            String address = "0x" + Keys.getAddress(keyPair.getPublicKey());

            return new WalletCredentials(address, encryptToBase64(privateKeyHex));
        } catch (Exception e) {
            throw new IllegalStateException("wallet key generation failed", e);
        }
    }

    /**
     * encryptToBase64로 저장된 private key payload를 복호화한다.
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

    /**
     * 암호화된 키 payload를 web3j Credentials 객체로 변환한다.
     */
    public Credentials credentialsFromEncrypted(String encryptedPrivateKey) {
        return Credentials.create(decryptPrivateKey(encryptedPrivateKey));
    }

    /**
     * AES-GCM 암호화 유틸.
     * 복호화 시 상태를 갖지 않도록 payload를 nonce + ciphertext 형태로 직렬화한다.
     */
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

    /**
     * 설정된 secret으로부터 고정 길이(32바이트) AES 키를 파생한다.
     */
    private byte[] sha256(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("sha-256 hash failed", e);
        }
    }

    /**
     * 바이트 배열을 prefix 없는 소문자 hex 문자열로 변환한다.
     */
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
