package com.merge.final_project.notification.email.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoundationRejectedEvent {
    private final Long foundationNo;
    private final String email;
    private final String foundationName;
    private final String rejectedReason;

}
