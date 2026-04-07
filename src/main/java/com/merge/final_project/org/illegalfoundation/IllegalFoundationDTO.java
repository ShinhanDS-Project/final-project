package com.merge.final_project.org.illegalfoundation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IllegalFoundationDTO {
    private String name;
    private String representative;
    private String reason;

    public static IllegalFoundationDTO from(IllegalFoundation illegalFoundation) {
        return IllegalFoundationDTO.builder()
                .name(illegalFoundation.getName())
                .representative(illegalFoundation.getRepresentative())
                .reason(illegalFoundation.getReason())
                .build();
    }

}
