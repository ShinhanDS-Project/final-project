package com.merge.final_project.notification.inapp;

public enum RecipientType {
    USERS, FOUNDATION, BENEFICIARY;

    public static RecipientType from(String role) {
        return switch (role) {
            case "ROLE_USER" -> USERS;
            case "ROLE_FOUNDATION" -> FOUNDATION;
            case "ROLE_BENEFICIARY" -> BENEFICIARY;
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
