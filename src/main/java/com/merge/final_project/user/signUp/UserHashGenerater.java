package com.merge.final_project.user.signUp;

import java.security.SecureRandom;

public class UserHashGenerater {
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int HASH_LENGTH = 4;
    private static final SecureRandom random = new SecureRandom();

    public static String generateUserHash() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < HASH_LENGTH; i++) {
            int index = random.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }

        return sb.toString();
    }
}
