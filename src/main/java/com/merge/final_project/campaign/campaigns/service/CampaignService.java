package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CampaignService {
    // 캠페인 비즈니스 로직 정의
    void registerCampaign(
            CampaignRequestDto requestDto,  // 캠페인 기본 정보
            MultipartFile imageFile,    // 대표 이미지 파일(1장)
            List<MultipartFile> detailImageFiles,   // 상세 페이지용(여러장)
            Long foundationNo   // 기부단체 PK
    );
}
