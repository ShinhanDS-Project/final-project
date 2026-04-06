package com.merge.final_project.org.illegalfoundation;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class IllegalFoundationResponseDTO {
    //정확히 일치하는 단체가 있는 경우 => 일치 여부와 단체 정보를 반환
    private boolean exactMatch;
    private IllegalFoundationDTO matchedFoundation;

    //유사한 단체가 있는 경우 => 리스트로 관리자도 볼 수 있도록 함. 없으면 빈 리스트
    private List<IllegalFoundationDTO> similarFoundations;

}
