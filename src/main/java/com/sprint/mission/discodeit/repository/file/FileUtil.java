package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.core.type.TypeReference; // 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList; // 추가
import java.util.List; // 추가

class FileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> void saveToFile(String filePath, T data) {
        saveToFile(new File(filePath), data);
    }

    public static <T> void saveToFile(File file, T data) {
        try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    // 기존 단일 객체 읽기 (필요 시 유지)
    public static <T> T readFromFile(String filePath, Class<T> valueType) {
        return readFromFile(new File(filePath), valueType);
    }

    public static <T> T readFromFile(File file, Class<T> valueType) {
        try {
            if (!file.exists()) return null;
            return objectMapper.readValue(file, valueType);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패", e);
        }
    }

    // [중요] 리스트 형태의 데이터를 읽기 위한 전용 메서드 추가
    // 레포지토리의 findAll() 등에서 List<User> 등을 가져올 때 사용합니다.
    public static <T> List<T> readListFromFile(String filePath, TypeReference<List<T>> typeReference) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return new ArrayList<>();
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("파일 리스트 읽기 실패", e);
        }
    }
}