package com.merge.final_project.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FoundationPasswordUpdateRequestDTO {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
