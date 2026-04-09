package com.merge.final_project.global.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {
    /**
     * 파일을 저장소에 저장합니다.
     * @return 저장된 유니크한 파일명 (storedName)
     */
    String saveFile(MultipartFile file) throws IOException;

    /**
     * 저장소에서 파일을 삭제합니다.
     * @param storedName 삭제할 파일명
     */
    void deleteFile(String storedName);

    /**
     * DB에 저장할 전체 경로(URL)를 생성합니다.
     * @param storedName 파일명
     * @return 로컬 경로 또는 S3 URL
     */
    String getFilePath(String storedName);
}
