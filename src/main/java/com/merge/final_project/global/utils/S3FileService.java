package com.merge.final_project.global.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

/**
 * 나중에 Amazon S3를 연동할 때 사용할 클래스입니다.
 * 실제 사용 시에는 클래스 상단에 @Service를 붙이고, LocalFileService의 @Service를 제거하세요.
 * 또한 build.gradle에 'software.amazon.awssdk:s3' 의존성 추가가 필요합니다.
 */
// @org.springframework.stereotype.Service 
public class S3FileService implements FileService {

    // 💡 나중에 본인의 S3 설정값으로 변경하세요.
    private final String bucketName = "your-bucket-name";
    private final String region = "ap-northeast-2"; // 서울 리전 예시

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + "_" + originalName;

        /* 
        TODO: AWS SDK를 이용한 업로드 로직 예시
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storedName)
                .contentType(file.getContentType())
                .build(), 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        */

        return storedName;
    }

    @Override
    public void deleteFile(String storedName) {
        if (storedName == null || storedName.isEmpty()) return;

        /* 
        TODO: AWS SDK를 이용한 삭제 로직 예시
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(storedName)
                .build());
        */
    }

    @Override
    public String getFilePath(String storedName) {
        // 💡 S3에 저장된 파일의 Public URL을 반환합니다.
        // 이 주소가 DB의 imgPath 컬럼에 저장됩니다.
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, storedName);
    }
}
