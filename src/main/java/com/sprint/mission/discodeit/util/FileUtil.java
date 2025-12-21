package com.sprint.mission.discodeit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> void saveToFile(String filePath, List<T> data) {
        try {
            File file = new File(filePath);
            // 디렉토리가 없으면 생성
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 데이터 저장
            objectMapper.writeValue(file, data);

            // [중요] 저장 직후 읽기가 시도되는 환경을 위해 물리적 쓰기 안정화 대기
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static <T> List<T> readFromFile(String filePath, Class<T> clazz) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            // 파일이 비어있거나 형식이 맞지 않는 경우 빈 리스트 반환
            return new ArrayList<>();
        }
    }
}