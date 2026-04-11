package com.merge.final_project.global.exceptions;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class NoRepeatedDigitsValidator implements ConstraintValidator<NoRepeatedDigits, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        int repeatCount = 1;
        char prev = 0;

        for (char ch : password.toCharArray()) {
            if (Character.isDigit(ch)) {
                if (ch == prev) {
                    repeatCount++;
                    if (repeatCount >= 3) {
                        return false;
                    }
                } else {
                    repeatCount = 1;
                }
                prev = ch;
            } else {
                repeatCount = 1;
                prev = 0;
            }
        }

        return true;
    }
}