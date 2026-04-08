package com.merge.final_project.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 경로 끝에 슬래시 보정
        String path = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        
        // /uploads/** 주소로 들어오면 환경변수로 설정된 폴더의 파일을 보여줍니다.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + path);
    }
}
