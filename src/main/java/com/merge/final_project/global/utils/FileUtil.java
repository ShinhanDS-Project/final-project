package com.merge.final_project.global.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 기존에 FileUtil을 사용하던 팀원들을 위한 유틸리티 클래스입니다.
 * 내부적으로는 FileService(현재 LocalFileService)를 호출하여 동작합니다.
 */
@Component
@RequiredArgsConstructor
public class FileUtil {

    private final FileService fileService;

    public String saveFile(MultipartFile file) throws IOException {
        return fileService.saveFile(file);
    }

    public void deleteFile(String storedName) {
        fileService.deleteFile(storedName);
    }

    // DB에 저장할 경로가 필요할 때 사용
    public String getFilePath(String storedName) {
        return fileService.getFilePath(storedName);
    }
}
