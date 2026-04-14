package com.merge.final_project.user.verify;

public record VerificationCodeIssueEvent(String email, String subject) {
}

