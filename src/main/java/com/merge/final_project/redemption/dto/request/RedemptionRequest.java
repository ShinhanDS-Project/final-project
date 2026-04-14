package com.merge.final_project.redemption.dto.request;

import com.merge.final_project.redemption.RequesterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder    //[가빈]
@AllArgsConstructor //[가빈] => 현금화 요청 시 기부단체에서 해당 클래스를 setter나 빌더로 값을 주입해야 하는데, 세터 보다는 빌더가 나을 거 같아서 수정함.
public class RedemptionRequest {

    private RequesterType requesterType;
    private Long requesterNo;
    private Long amount;
}
