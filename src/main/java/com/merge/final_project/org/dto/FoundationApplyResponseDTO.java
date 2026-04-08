package com.merge.final_project.org.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoundationApplyResponseDTO {

    private String foundationEmail;
    private String foundationName;
    private String representativeName;
}
