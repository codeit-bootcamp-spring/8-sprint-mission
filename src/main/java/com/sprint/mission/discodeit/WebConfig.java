package com.sprint.mission.discodeit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload.windows.path}")
    private String windowsPath;

    @Value("${file.upload.mac.path}")
    private String macPath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api",
                c -> c.isAnnotationPresent(RestController.class));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String osName = System.getProperty("os.name").toLowerCase();
        String uploadPath;

        if(osName.contains("win")) {
            uploadPath = windowsPath;
        } else {
            uploadPath = macPath;
        }
        /*
        실제 업로드된 파일에 접근할 URL 패턴과 실제 파일 시스템 경로 매핑
        file:// 프로토콜은 OS의 파일 시스템에 접근하는 프로토콜
         */
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadPath);
    }
}
