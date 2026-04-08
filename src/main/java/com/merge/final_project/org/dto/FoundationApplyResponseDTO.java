package com.merge.final_project.org.dto;

import com.merge.final_project.org.FoundationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class FoundationApplyResponseDTO {

    private String foundationEmail;
    private String foundationName;
    private String representativeName;
}
