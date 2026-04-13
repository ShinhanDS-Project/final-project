package com.merge.final_project.report.finalreport.service;

import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.utils.FileService;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FinalReportService {

    private final FinalReportRepository finalReportRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CampaignRepository campaignRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService; // 💡 인터페이스 주입
    private final ApplicationEventPublisher eventPublisher; //[가빈] 활동 보고서 등록 시 관리자 화면에서 볼 수 있게 이벤트 발생

    @Transactional(rollbackFor = Exception.class)
    public void saveFullReport(FinalReportRequestDTO dto, List<MultipartFile> files, List<String> purposes, String email) throws IOException {
        validateImages(files);
        FinalReport report = saveReportEntity(dto, email);
        saveImageEntities(report, files, purposes);

        //[가빈]어떤 수혜자가 쓴 활동 보고서인지 관리자 화면에서 확인할 수 있게 이벤트 발생
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        eventPublisher.publishEvent(new ApprovalRequestEvent(
                TargetType.FINAL_REPORT, report.getReportNo(), beneficiary.getName() + "의 활동 보고서 승인 요청"
        ));
    }

    private void validateImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
            throw new RuntimeException("보고서 제출 시 최소 1장 이상의 사진은 필수입니다.");
        }
    }

    private FinalReport saveReportEntity(FinalReportRequestDTO dto, String email) {
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        boolean isValid = campaignRepository.existsByCampaignNoAndBeneficiaryNo(
                dto.getCampaign_no(), beneficiary.getBeneficiaryNo());

        if (!isValid) {
            throw new RuntimeException("해당 캠페인에 대한 보고서 작성 권한이 없습니다.");
        }

        FinalReport report = FinalReport.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .usagePurpose(dto.getUsagePurpose())
                .campaign_no(dto.getCampaign_no())
                .beneficiary_no(beneficiary.getBeneficiaryNo())
                .approvalStatus(ReportApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return finalReportRepository.save(report);
    }

    private void saveImageEntities(FinalReport report, List<MultipartFile> files, List<String> purposes) throws IOException {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String purpose = (purposes != null && purposes.size() > i) ? purposes.get(i) : "GENERAL";

            //  인터페이스의 saveFile 사용
            String storedName = fileService.saveFile(file);

            Image imageEntity = Image.builder()
                    .imgOrgName(file.getOriginalFilename())
                    .imgStoredName(storedName)
                    .imgPath(fileService.getFilePath(storedName)) // 💡 인터페이스의 getFilePath 사용
                    .targetName("final_report")
                    .targetNo(report.getReportNo())
                    .purpose(purpose)
                    .createdAt(LocalDateTime.now())
                    .build();

            imageRepository.save(imageEntity);
        }
    }

    @Transactional(readOnly = true)
    public List<FinalReportResponseDTO> getMyReports(String email) {
        List<FinalReport> reports = finalReportRepository.findByBeneficiaryEmail(email);
        return reports.stream().map(report -> {
            List<Image> images = imageRepository.findByTargetNameAndTargetNo("final_report", report.getReportNo());
            return new FinalReportResponseDTO(report, images);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FinalReportResponseDTO getReportDetail(Long reportNo, String email) {
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new RuntimeException("해당 보고서를 찾을 수 없습니다."));

        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        if (!report.getBeneficiary_no().equals(beneficiary.getBeneficiaryNo())) {
            throw new RuntimeException("해당 보고서를 조회할 권한이 없습니다.");
        }

        List<Image> images = imageRepository.findByTargetNameAndTargetNo("final_report", reportNo);
        return new FinalReportResponseDTO(report, images);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateReport(Long reportNo, FinalReportRequestDTO dto,
                             List<MultipartFile> files, List<String> purposes, String email) throws IOException {

        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new RuntimeException("수정할 보고서가 없습니다."));

        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        if (!report.getBeneficiary_no().equals(beneficiary.getBeneficiaryNo())) {
            throw new RuntimeException("해당 보고서를 수정할 권한이 없습니다.");
        }

        report.updateContent(dto.getTitle(), dto.getContent(), dto.getUsagePurpose());

        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            List<Image> oldImages = imageRepository.findByTargetNameAndTargetNo("final_report", reportNo);
            for (Image img : oldImages) {
                fileService.deleteFile(img.getImgStoredName()); // 💡 인터페이스의 deleteFile 사용
            }
            imageRepository.deleteByTargetNameAndTargetNo("final_report", reportNo);
            saveImageEntities(report, files, purposes);
        }
    }

    /**
     * 보고서의 승인 상태 변경. (관리자 전용)
     */
    @Transactional
    public void updateReportStatus(Long reportNo, ReportApprovalStatus newStatus, String rejectReason) {
        // 1. 대상 보고서를 찾습니다. 없으면 에러를 발생시킵니다.
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new RuntimeException("해당 보고서(번호: " + reportNo + ")를 찾을 수 없습니다."));

        // 2. 반려(REJECTED)인 경우 사유가 있는지 검증합니다.
        if (newStatus == ReportApprovalStatus.REJECTED) {
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                throw new RuntimeException("반려 시에는 반드시 사유를 입력해야 합니다.");
            }
        }

        // 3. 엔티티의 상태를 변경.
        report.changeStatus(newStatus, rejectReason);

        log.info("보고서 번호 {}의 상태가 {}로 변경되었습니다.", reportNo, newStatus);
    }

    //[이채원] --> 예외 캠페인에서 사업종료일 다음날 00시 00분 이 지났고, 리포트 제추안한거 (캠페인 상태 ,
    // localdateTime.now()이 사업종료일 그다음날 00시 00분보다 크면 00일 지났습니다로 조회)
    //있으면 조회해오기

    public FinalReport showReport(Long campaignNo){
        //1. 실제로 존재하는 캠페인인지 확인
        Campaign campaign= campaignRepository.findByCampaignNo(campaignNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        //2. 리포트 여부 확인
        FinalReport finalReport= finalReportRepository.findByCampaignNo(campaignNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.FINAL_REPORT_NOT_FOUND));

        //3. 사업 종료일 확인
        // 캠페인 엔티티에서 사업종료일+1 가져오기
        LocalDateTime usageEndAt= campaign.getUsageEndAt();

        // 기준은 종료일 다음날 00시 00분 설정이므로
        // 종료일 다음날 00시 00분 설정
        LocalDateTime nextDayMidnight = usageEndAt.plusDays(1).toLocalDate().atStartOfDay();
        if(LocalDateTime.now().isAfter(nextDayMidnight)){
                // 몇일 지났는지 확인해야함
                int day= nextDayMidnight.minusDays(LocalDateTime.now());
        }
        // 3. 모든 조건을 통과하면 리포트 반환 (이미지에 있던 'Missing return statement' 해결)
        return finalReport;
    }

}
