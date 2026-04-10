package com.merge.final_project.global;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDTO {
    private String imgOrgName;
    private String imgStoredName;
    private String imgPath;
    private String purpose;

    public ImageDTO(Image entity) {
        this.imgOrgName = entity.getImgOrgName();
        this.imgStoredName = entity.getImgStoredName();
        this.imgPath = entity.getImgPath();
        this.purpose = entity.getPurpose();
    }
}