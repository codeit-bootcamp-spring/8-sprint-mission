package com.sprint.mission.discodeit.util;

import com.fasterxml.jackson.core.type.TypeReference; // 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // 경로(String) 지원을 위한 오버로딩
    public static <T> void saveToFile(String filePath, T data) {
        saveToFile(new File(filePath), data);
    }

    public static <T> void saveToFile(File file, T data) {
        try {
            // 디렉토리가 없으면 생성
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            // [멘토 피드백 반영] Thread.sleep(100) 제거
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + file.getAbsolutePath(), e);
        }
    }

    //  리스트 형태의 데이터를 읽기 위한 전용 메서드 추가
    public static <T> List<T> readListFromFile(String filePath, TypeReference<List<T>> typeReference) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return new ArrayList<>();
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("파일 리스트 읽기 실패", e);
        }
    }

    public static <T> T readFromFile(String filePath, Class<T> valueType) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;
            return objectMapper.readValue(file, valueType);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }
}