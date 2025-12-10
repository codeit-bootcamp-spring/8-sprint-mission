package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FileUserRepository implements UserRepository {

    private static final String DATA_DIR = "data";
    private static final String USER_DIR = "user";
    private static final String DATA_FILE = DATA_DIR + File.separator + USER_DIR + File.separator + "users.ser";

    @Override
    public User save(User user) {

        // 파일에서 기존 User 목록 읽기 (역직렬화)
        List<User> users = loadUsersFromFile();

        // 기존에 같은 ID 있으면 제거 후 새로 넣기
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);

        // 전체 목록을 다시 파일에 저장 (직렬화)
        saveUsersToFile(users);

        return user;
    }

    @Override
    public User findById(UUID id) {
        return loadUsersFromFile().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(loadUsersFromFile());
    }

    @Override
    public List<User> findAllGender(String gender) {
        return loadUsersFromFile().stream()
                .filter(u -> gender.equals(u.getGender()))
                .toList();
    }

    @Override
    public List<User> findAllAge() {
        return loadUsersFromFile().stream()
                .sorted(Comparator.comparing(User::getAge))
                .toList();
    }

    @Override
    public User update(UUID id, User updateUser) {
        // updateUser는 이미 Service에서 수정된 객체라고 가정
        List<User> users = loadUsersFromFile();

        // 기존 데이터 제거 후 새 객체로 교체
        users.removeIf(u -> u.getId().equals(id));
        users.add(updateUser);

        saveUsersToFile(users);
        return updateUser;
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadUsersFromFile();
        users.removeIf(u -> u.getId().equals(id));
        saveUsersToFile(users);
    }

    private File getDataFile() {

        File dir = new File(DATA_DIR + File.separator + USER_DIR);

        if (!dir.exists()) {
            dir.mkdirs(); // data/user 디렉터리 없으면 생성
        }

        return new File(DATA_FILE);
    }

    private List<User> loadUsersFromFile() {

        File file = getDataFile();

        // 파일이 없으면 빈 리스트 반환
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<User>) ois.readObject();
        } catch (Exception e) {
            // 역직렬화 실패 시 빈 리스트
            return new ArrayList<>();
        }
    }

    private void saveUsersToFile(List<User> users) {
        File file = getDataFile();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }


}
