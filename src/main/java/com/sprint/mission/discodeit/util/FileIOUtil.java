package com.sprint.mission.discodeit.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 경고 억제: 제네릭 타입 캐스팅 시 발생하는 경고를 무시합니다.
@SuppressWarnings("unchecked")
public class FileIOUtil {

    /**
     * 지정된 파일 경로에서 Map<UUID, T> 형태의 객체를 역직렬화하여 읽어옵니다.
     * 파일이 없거나 비어있는 경우 빈 HashMap을 반환합니다.
     * * @param filePath 읽어올 파일의 경로 (예: "data/user.json")
     * @param <T> Map의 값 타입 (예: User, Channel 등)
     * @return 파일에서 읽어온 데이터 Map
     * @throws IOException 파일 입출력 중 오류가 발생할 경우
     * @throws ClassNotFoundException 역직렬화 중 클래스를 찾지 못할 경우
     */
    public static <T> Map<UUID, T> readObjectFromFile(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);

        // 1. 파일이 존재하지 않거나 비어있는 경우, 빈 HashMap 반환
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        // 2. ObjectInputStream을 사용하여 객체 역직렬화 (읽기)
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, T>) ois.readObject();
        }
        // 메서드 호출부(Repository)에서 예외를 처리하도록 throws 선언합니다.
    }

    /**
     * Map<UUID, T> 형태의 데이터를 지정된 파일 경로에 직렬화하여 저장합니다.
     * * @param filePath 저장할 파일의 경로 (예: "data/user.json")
     * @param data 파일에 저장할 데이터 Map
     * @param <T> Map의 값 타입
     * @throws IOException 파일 입출력 중 오류가 발생할 경우
     */
    public static <T> void writeObjectToFile(String filePath, Map<UUID, T> data) throws IOException {

        // 1. 파일이 위치할 디렉터리가 없으면 생성 (예: 'data/' 폴더가 없으면 생성)
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // 디렉토리 생성
        }

        // 2. ObjectOutputStream을 사용하여 객체 직렬화 (쓰기)
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        }
    }
}