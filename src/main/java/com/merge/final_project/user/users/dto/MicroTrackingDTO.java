package com.merge.final_project.user.users.dto;

import com.merge.final_project.campaign.settlement.dto.SelectSettlementResponseDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportMicroTrackingResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MicroTrackingDTO {
    SelectSettlementResponseDTO UsersettlementDTO;
    FinalReportMicroTrackingResponseDto UserfinalReportDTO;
}
