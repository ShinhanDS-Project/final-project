package com.merge.final_project.global.utils;

public class MaskingUtils {/**
 * 이메일 마스킹 처리 (앞 두 글자만 남기고 마스킹)
 * 예: chae-won@gmail.com -> ch*******@gmail.com
 */
public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
        return email;
    }

    // 1. @ 기준으로 ID와 도메인 분리
    String[] parts = email.split("@");
    String idPart = parts[0];
    String domainPart = parts[1];

    // 2. ID 마스킹 로직
    StringBuilder maskedId = new StringBuilder();
    if (idPart.length() <= 2) {
        // ID가 2글자 이하라면 첫 글자만 남기고 마스킹
        maskedId.append(idPart.charAt(0));
        for (int i = 1; i < idPart.length(); i++) {
            maskedId.append("*");
        }
    } else {
        // 앞 2글자 남기고 나머지는 '*' 처리
        maskedId.append(idPart, 0, 2);
        for (int i = 2; i < idPart.length(); i++) {
            maskedId.append("*");
        }
    }

    // 3. 다시 합쳐서 반환
    return maskedId.append("@").append(domainPart).toString();
}
}
