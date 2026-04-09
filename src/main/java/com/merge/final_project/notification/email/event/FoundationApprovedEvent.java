package com.merge.final_project.notification.email.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class FoundationApprovedEvent {
    private final Long foundationNo;
    private final String email;
    private final String foundationName;
    private final String tempPassword;
}
