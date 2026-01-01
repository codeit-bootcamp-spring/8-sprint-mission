package com.sprint.mission.discodeit;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;

@Component
public class FileUploadUtils {
    @Value("${file.upload.windows.path}")
    private String windowsPath;

    @Value("${file.upload.mac.path}")
    private String macPath;

    public String getUploadPath(String subDirectory) {
        String osName = System.getProperty("os.name").toLowerCase();
        String basePath;

        if(osName.contains("win")) {
            basePath = windowsPath;
        } else {
            basePath = macPath;
        }

        String uploadPath = basePath + "/" + subDirectory;
        File directory = new File(uploadPath);

        if (!directory.exists()) {
            System.out.println("파일 업로드를 위한 디렉토리 생성!");

            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("파일 업로드 디렉토리 생성 실패: " + uploadPath);
            }
        }

        return uploadPath;
    }
}
