package com.merge.final_project.admin.adminlog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "관리자 활동 로그 응답 DTO")
@Getter
@Builder
public class AdminLogResponseDTO {

    @Schema(description = "로그 번호", example = "1")
    private Long logNo;

    @Schema(description = "관리자 아이디", example = "admin01")
    private String adminId;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String adminName;

    @Schema(description = "행동 유형 (APPROVE, REJECT, ACTIVATE, DEACTIVATE 등)", example = "APPROVE")
    private ActionType actionType;

    @Schema(description = "대상 유형 (FOUNDATION, CAMPAIGN, REPORT, USER 등)", example = "FOUNDATION")
    private TargetType targetType;

    @Schema(description = "대상 엔티티 번호", example = "5")
    private Long targetNo;

    @Schema(description = "활동 설명", example = "기부단체 승인 처리")
    private String description;

    @Schema(description = "로그 생성 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    public static AdminLogResponseDTO from(AdminLog log) {
        return AdminLogResponseDTO.builder()
                .logNo(log.getLogNo())
                .adminId(log.getAdmin().getAdminId())
                .adminName(log.getAdmin().getName())
                .actionType(log.getActionType())
                .targetType(log.getTargetType())
                .targetNo(log.getTargetNo())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
