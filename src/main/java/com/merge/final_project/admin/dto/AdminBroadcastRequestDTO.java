package com.merge.final_project.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminBroadcastRequestDTO {
    @NotBlank(message = "공지 내용은 필수입니다.")
    @Size(max = 500, message = "공지 내용은 500자를 초과할 수 없습니다.")
    private String content;
}
