package com.merge.final_project.user.verify.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVerifyResponseDTO {
    private boolean success;
    private boolean available;
    private String message;
}
