package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

    private static final String DATA_DIR = "data";
    private static final String MESSAGE_DIR = "message";
    private static final String DATA_FILE = DATA_DIR + File.separator + MESSAGE_DIR + File.separator + "messages.ser";


    @Override
    public Message save(Message message) {

        // 파일에서 기존 Message 목록 읽기 (역직렬화)
        List<Message> messages = loadMessagesFromFile();

        // 기존에 같은 ID 있으면 제거 후 새로 넣기
        messages.removeIf(m -> m.getId().equals(message.getId()));
        messages.add(message);

        // 전체 목록을 다시 파일에 저장 (직렬화)
        saveMessagesToFile(messages);

        return message;
    }

    @Override
    public Message findById(UUID id) {
        return loadMessagesFromFile().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(loadMessagesFromFile());
    }

    @Override
    public Message update(UUID id, Message updateMessage) {
        // updateMessage 이미 Service에서 수정된 객체라고 가정
        List<Message> messages = loadMessagesFromFile();

        // 기존 데이터 제거 후 새 객체로 교체
        messages.removeIf(m -> m.getId().equals(id));
        messages.add(updateMessage);

        saveMessagesToFile(messages);
        return updateMessage;
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadMessagesFromFile();
        messages.removeIf(m -> m.getId().equals(id));
        saveMessagesToFile(messages);
    }

    private File getDataFile() {

        File dir = new File(DATA_DIR + File.separator + MESSAGE_DIR);

        if (!dir.exists()) {
            dir.mkdir(); // data/message 디렉터리 없으면 생성
        }

        return new File(DATA_FILE);
    }

    private List<Message> loadMessagesFromFile() {

        File file = getDataFile();

        // 파일이 없으면 빈 리스트 반환
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Message>) ois.readObject();
        } catch (Exception e) {
            // 역직렬화 실패 시 빈 리스트
            return new ArrayList<>();
        }
    }

    private void saveMessagesToFile(List<Message> messages) {
        File file = getDataFile();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {

        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
