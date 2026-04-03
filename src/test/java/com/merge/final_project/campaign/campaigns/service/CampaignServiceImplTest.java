package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CampaignServiceImplTest {

    @Autowired
    private CampaignService campaignService;

    @Test
    void registerCampaignTest() {
        // 가짜 요청 데이터 준비
        CampaignRequestDto dto = new CampaignRequestDto();
        dto.setTitle("테스트 캠페인");
        dto.setDescription("테스트 설명");
        dto.setCategory("CHILD");
        dto.setTargetAmount(1000000L);
        dto.setBeneficiaryNo(2L);

        UsePlanRequestDto plan = new UsePlanRequestDto();
        plan.setPlanContent("식비");
        plan.setPlanAmount(500000);
        dto.setUsePlans(List.of(plan));

        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, foundationNo);

        System.out.println("캠페인 등록 성공");
    }


}
