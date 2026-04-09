package com.merge.final_project.global.utils;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {
    // 💡 파일을 저장할 경로
    //@Value("${file.upload.path}")
    private String uploadPath = "c:/upload";

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

    public void deleteFile(String savedPath) {
        // 1. 경로가 없거나 기본 이미지인 경우 삭제 로직 건너뛰기
        if (savedPath == null || savedPath.isBlank() || ("default-profile.png").equals(savedPath)) {
            return;
        }

        try {
            Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path target = base.resolve(savedPath).normalize();

            // 2. 업로드 루트 밖 삭제 방지 (Path Traversal 방어)
            if (!target.startsWith(base)) {
                // ErrorCode에 INVALID_FILE_PATH(400) 등을 추가해서 사용 권장
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Files.deleteIfExists(target);
        } catch (IOException e) {
            // 파일 삭제 실패는 서버 오류이므로 500 에러 코드 사용
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}