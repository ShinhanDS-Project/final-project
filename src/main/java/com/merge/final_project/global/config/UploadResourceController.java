package com.merge.final_project.global.config;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadResourceController {

    @Value("${file.upload-path:${file.upload.path:C:/uploads/}}")
    private String uploadPath;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveUpload(HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String prefix = "/uploads/";
        int start = uri.indexOf(prefix);
        if (start < 0) {
            return ResponseEntity.notFound().build();
        }

        String relativePath = uri.substring(start + prefix.length());
        if (relativePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root) || !Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(target);
        MediaType mediaType = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .contentType(mediaType)
                .body(new FileSystemResource(target));
    }
}
