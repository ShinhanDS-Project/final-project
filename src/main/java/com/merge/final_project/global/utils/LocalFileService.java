package com.merge.final_project.global.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

//@Service
public class LocalFileService implements FileService {

    @org.springframework.beans.factory.annotation.Value("${file.upload-path}")
    private String uploadPath;

    // 슬래시 중복 방지 및 경로 정규화 도우미
    private String getNormalizedPath() {
        if (uploadPath == null) return "C:/uploads/";
        return uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String path = getNormalizedPath();
        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + "_" + originalName;

        File saveFile = new File(path, storedName);
        file.transferTo(saveFile);

        return storedName;
    }

    @Override
    public void deleteFile(String storedName) {
        if (storedName == null || storedName.isEmpty()) return;

        File file = new File(getNormalizedPath(), storedName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public String getFilePath(String storedName) {
        return getNormalizedPath() + storedName;
    }
}
