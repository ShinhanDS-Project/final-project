package com.merge.final_project.report.finalreport.service;

import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.notification.email.event.FoundationInactiveEvent;
import com.merge.final_project.notification.inapp.NotificationRepository;
import com.merge.final_project.notification.inapp.NotificationService;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import com.merge.final_project.org.FoundationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// [가빈] 활동 보고서 미제출 수혜자들에게 알림 보내기 위한 서비스 => 기존 활동보고서 패키지 구조에 맞춰 서비스 클래스에 구현.
@Service
@RequiredArgsConstructor
@Slf4j
public class FinalReportReminderService {

    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final FoundationRepository foundationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reminderOver7Days() {
        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> campaigns = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        for (Campaign campaign : campaigns) {
            // 이미 7일 알림을 받은 수혜자는 skip (중복 발송 방지)
            if (notificationRepository.existsByReceiverNoAndNotificationType(
                    campaign.getBeneficiaryNo(), NotificationType.FINAL_REPORT_DUE_WARNING)) {
                log.info("7일 알림 중복 skip campaignNo={}", campaign.getCampaignNo());
                continue;
            }

            // 수혜자 - 보고서 제출 목록
            notificationService.send(
                    RecipientType.BENEFICIARY,
                    campaign.getBeneficiaryNo(),
                    NotificationType.FINAL_REPORT_DUE_WARNING,
                    "[" + campaign.getTitle() + "] 활동 보고서 제출이 지연되고 있습니다.\n" +
                            "사업 집행 종료일 기준 7일이 경과하였습니다. 빠른 제출을 부탁드립니다.\n\n" +
                            "14일 이상 미제출 시 계정이 비활성화되어 서비스 이용이 제한될 수 있습니다."
            );

            //기부단체 - 수혜자 불성실 안내
            notificationService.send(
                    RecipientType.FOUNDATION,
                    campaign.getFoundationNo(),
                    NotificationType.FINAL_REPORT_DUE_WARNING,
                    "[" + campaign.getTitle() + "] 선정하신 수혜자의 활동 보고서 제출 기한이 7일 경과되었습니다."
            );

            log.info("7일 경과 알림 발송 campaignNo={}", campaign.getCampaignNo());
        }
    }

    @Transactional
    public void remindOver14Days() {
        LocalDateTime cutoff14 = LocalDate.now().minusDays(14).atStartOfDay();
        List<Campaign> campaigns = campaignRepository
                .findSettledCampaignsWithNoReport(cutoff14);

        for (Campaign campaign : campaigns) {
            // 이미 14일 알림(비활성화)을 처리한 캠페인은 skip (중복 처리 방지)
            if (notificationRepository.existsByReceiverNoAndNotificationType(
                    campaign.getBeneficiaryNo(), NotificationType.FINAL_REPORT_OVERDUE)) {
                log.info("14일 알림 중복 skip campaignNo={}", campaign.getCampaignNo());
                continue;
            }

            // 수혜자 — 미제출 경고
            notificationService.send(
                    RecipientType.BENEFICIARY,
                    campaign.getBeneficiaryNo(),
                    NotificationType.FINAL_REPORT_OVERDUE,
                    "[" + campaign.getTitle() + "] 활동 보고서가 14일 이상 미제출 상태입니다."
            );

            // 기부단체 — 수혜자 불성실 안내
            notificationService.send(
                    RecipientType.FOUNDATION,
                    campaign.getFoundationNo(),
                    NotificationType.FINAL_REPORT_OVERDUE,
                    "[" + campaign.getTitle() + "] 선정하신 수혜자의 활동 보고서 미제출로 인해 계정이 비활성화됩니다."
            );

            // 관리자 SSE
            eventPublisher.publishEvent(new ApprovalRequestEvent(
                    TargetType.CAMPAIGN,
                    campaign.getCampaignNo(),
                    "[" + campaign.getTitle() + "] 활동 보고서 14일 미제출 — 단체 비활성화"
            ));

            // 단체 비활성화 + 메일 (AFTER_COMMIT 이벤트로 발송)
            foundationRepository.findById(campaign.getFoundationNo())
                    .ifPresent(foundation -> {
                        foundation.deactivate();
                        eventPublisher.publishEvent(new FoundationInactiveEvent(
                                foundation.getFoundationEmail(),
                                foundation.getFoundationName(),
                                campaign.getTitle()
                        ));
                    });

            log.info("14일 경과 처리 campaignNo={}, foundationNo={}", campaign.getCampaignNo(), campaign.getFoundationNo());
        }
    }
}
