package com.merge.final_project.donation.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossResponse<T>{
    private String version;
    private String traceId;
    private String entityType; // 식별자 역할
    private T entityBody;
}
