package com.merge.final_project.user.verify;

public record VerificationEvent(String email, String subject, String code) {}
