package com.merge.final_project.report.finalreport.service;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinalReportService {

    private final FinalReportRepository finalReportRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CampaignRepository campaignRepository;
    private final ImageRepository imageRepository;
    private final FileUtil fileUtil;


    @Transactional(rollbackFor = Exception.class)
    public void saveFullReport(FinalReportRequestDTO dto, List<MultipartFile> files,List<String> purposes, String email) throws IOException {

        // 1. 필수 검증: 사진이 없으면 진행 불가
        validateImages(files);

        // 2. 보고서 본문 저장
        FinalReport report = saveReportEntity(dto, email);

        // 3. 이미지 파일 및 정보 저장
        saveImageEntities(report, files, purposes);
    }

    // --- 아래는 내부 세부 공정 (Private) ---

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

            // 목적(purpose) 매칭: 리스트가 부족하면 "GENERAL"로 기본값 부여
            String purpose = (purposes != null && purposes.size() > i) ? purposes.get(i) : "GENERAL";

            String storedName = fileUtil.saveFile(file);

            Image imageEntity = Image.builder()
                    .imgOrgName(file.getOriginalFilename())
                    .imgStoredName(storedName)
                    .imgPath("C:/uploads/reports/" + storedName)
                    .targetName("final_report")
                    .targetNo(report.getReportNo())
                    .purpose(purpose) // 💡 목적 저장
                    .createdAt(LocalDateTime.now())
                    .build();

            imageRepository.save(imageEntity);
        }
    }

    @Transactional(readOnly = true)
    public List<FinalReportResponseDTO> getMyReports(String email) {
        // 1. 해당 수혜자의 보고서 목록 조회
        List<FinalReport> reports = finalReportRepository.findByBeneficiaryEmail(email);

        // 2. 각 보고서마다 이미지를 찾아서 DTO로 변환
        return reports.stream().map(report -> {
            // 해당 보고서의 이미지들 조회 (target_name = "final_report", target_no = reportNo)
            List<Image> images = imageRepository.findByTargetNameAndTargetNo("final_report", report.getReportNo());

            return new FinalReportResponseDTO(report, images);
        }).collect(Collectors.toList());
    }
    /**
     * 보고서 상세 조회 (보고서 본문 + 이미지 리스트) - 작성자 본인만 가능
     */
    @Transactional(readOnly = true)
    public FinalReportResponseDTO getReportDetail(Long reportNo, String email) {

        // 1. 보고서 본문 찾기 (없으면 에러)
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new RuntimeException("해당 보고서를 찾을 수 없습니다."));

        // 2. 작성자 본인 확인
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        if (!report.getBeneficiary_no().equals(beneficiary.getBeneficiaryNo())) {
            throw new RuntimeException("해당 보고서를 조회할 권한이 없습니다.");
        }

        // 3. 이 보고서에 달린 이미지들 찾기 (글로벌 규격: target_name, target_no 활용)
        List<Image> images = imageRepository.findByTargetNameAndTargetNo("final_report", reportNo);

        // 4. DTO로 변환해서 반환
        return new FinalReportResponseDTO(report, images);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateReport(Long reportNo, FinalReportRequestDTO dto,
                             List<MultipartFile> files, List<String> purposes, String email) throws IOException {

        // 1. 기존 보고서 존재 확인 및 권한 검증
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new RuntimeException("수정할 보고서가 없습니다."));

        Beneficiary beneficiary = beneficiaryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("수혜자 정보를 찾을 수 없습니다."));

        // 작성자(수혜자 번호) 비교를 통한 권한 체크
        if (!report.getBeneficiary_no().equals(beneficiary.getBeneficiaryNo())) {
            throw new RuntimeException("해당 보고서를 수정할 권한이 없습니다.");
        }

        // 2. 보고서 본문 내용 업데이트 (Dirty Checking 활용)
        report.updateContent(dto.getTitle(), dto.getContent(), dto.getUsagePurpose());

        // 3. 새 사진이 들어온 경우에만 기존 사진 교체 (물리 파일 삭제 포함)
        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            // 3-1. 기존 이미지 목록 조회하여 실제 파일 삭제
            List<Image> oldImages = imageRepository.findByTargetNameAndTargetNo("final_report", reportNo);
            for (Image img : oldImages) {
                fileUtil.deleteFile(img.getImgStoredName());
            }

            // 3-2. 기존 DB 정보 삭제
            imageRepository.deleteByTargetNameAndTargetNo("final_report", reportNo);

            // 3-3. 새 사진들 저장
            saveImageEntities(report, files, purposes);
        }
    }
}
