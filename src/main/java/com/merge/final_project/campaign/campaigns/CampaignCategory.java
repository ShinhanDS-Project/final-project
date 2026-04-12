package com.merge.final_project.campaign.campaigns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CampaignCategory {
    CHILD_YOUTH("아동/청소년"),
    SENIOR("어르신"),
    DISABLED("장애인"),
    ANIMAL("동물"),
    ENVIRONMENT("환경"),
    ETC("기타");

    private final String label;
}
