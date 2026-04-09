package com.merge.final_project.redemption.dto.request;

import com.merge.final_project.redemption.RequesterType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RedemptionRequest {

    private RequesterType requesterType;
    private Long requesterNo;
    private Long amount;
}
