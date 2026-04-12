package com.merge.final_project.report.finalreport.scheduler;

import com.merge.final_project.report.finalreport.service.FinalReportReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinalReportReminderScheduler {

    private final FinalReportReminderService finalReportReminderService;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        finalReportReminderService.reminderOver7Days();
        finalReportReminderService.remindOver14Days();
    }
}
