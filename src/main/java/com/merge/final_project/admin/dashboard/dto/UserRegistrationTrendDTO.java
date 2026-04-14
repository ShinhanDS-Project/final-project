package com.merge.final_project.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegistrationTrendDTO {

    private String date;   // "YYYY-MM-DD"
    private long count;
}
