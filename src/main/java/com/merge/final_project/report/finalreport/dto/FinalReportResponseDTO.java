package com.merge.final_project.report.finalreport.dto;

import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.global.Image;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalReportResponseDTO {

    private Long reportNo;
    private String title;
    private String content;
    private String usagePurpose;
    private Long campaignNo;
    private ReportApprovalStatus approvalStatus;
    private LocalDateTime createdAt;

    // 💡 이 보고서에 첨부된 이미지 리스트 (중요!)
    private List<ImageDTO> images;

    /**
     * Entity를 DTO로 변환하는 생성자
     */
    public FinalReportResponseDTO(FinalReport entity, List<Image> imageEntities) {
        this.reportNo = entity.getReportNo();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.usagePurpose = entity.getUsagePurpose();
        this.campaignNo = entity.getCampaign_no();
        this.approvalStatus = entity.getApprovalStatus();
        this.createdAt = entity.getCreatedAt();

        // Image 엔티티 리스트를 내부 ImageDTO 리스트로 변환
        this.images = imageEntities.stream()
                .map(ImageDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 이미지 정보만 따로 담을 내부 클래스 (필요한 정보만 노출)
     */
    @Getter
    @NoArgsConstructor
    public static class ImageDTO {
        private String imgOrgName;
        private String imgStoredName;
        private String imgPath;

        public ImageDTO(Image entity) {
            this.imgOrgName = entity.getImgOrgName();
            this.imgStoredName = entity.getImgStoredName();
            this.imgPath = entity.getImgPath();
        }
    }
}