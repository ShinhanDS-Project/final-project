package com.merge.final_project.notification.email.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FoundationInactiveEvent {
    private final String email;
    private final String foundationName;
    private final String campaignTitle;
}
