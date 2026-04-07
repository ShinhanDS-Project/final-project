package com.merge.final_project.global.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtil {
    // 💡 파일을 저장할 경로
    private final String uploadPath = "C:/uploads/";

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // 1. 폴더 생성
        File folder = new File(uploadPath);
        if (!folder.exists()) folder.mkdirs();

        // 2. 중복 방지 이름 생성 (UUID)
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + "_" + originalName;

        // 3. 물리적 저장
        File saveFile = new File(uploadPath, storedName);
        file.transferTo(saveFile);

        return storedName; // DB에 저장할 이름을 반환
    }

    /**
     * 실제 물리 파일을 삭제합니다.
     */
    public void deleteFile(String storedName) {
        if (storedName == null || storedName.isEmpty()) return;

        File file = new File(uploadPath, storedName);
        if (file.exists()) {
            file.delete();
        }
    }
}