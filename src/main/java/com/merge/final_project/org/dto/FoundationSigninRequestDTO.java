package com.merge.final_project.org.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FoundationSigninRequestDTO {

    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
