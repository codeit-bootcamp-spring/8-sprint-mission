package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import java.util.*;
import java.util.stream.Collectors;

public class JCFUserService implements UserService {

    // 싱글톤 패턴 구현: 정적 인스턴스
    private static JCFUserService INSTANCE;

    // JCF 필드: 데이터를 저장할 Map (ID -> Entity)
    private final Map<UUID, User> data;

    // private 생성자: 외부에서 직접 인스턴스 생성을 막음
    private JCFUserService() {
        this.data = new HashMap<>();
    }

    // 싱글톤 인스턴스 반환 메서드
    public static JCFUserService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JCFUserService();
        }
        return INSTANCE;
    }

    // --- CRUD 구현 ---

    @Override
    public User save(User user) {
        // 생성/업데이트 시 데이터를 맵에 저장
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        // 단건 조회: Optional로 null 안전성 확보
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        // 다건 조회: Stream API를 사용하여 Map의 값들을 List로 변환
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public User update(User user) {
        // 수정: 엔티티의 ID가 존재할 경우 덮어쓰고, 수정된 엔티티 반환
        if (data.containsKey(user.getId())) {
            data.put(user.getId(), user);
            return user;
        }
        // ID가 존재하지 않으면 예외 발생
        throw new NoSuchElementException("수정할 User ID가 존재하지 않습니다: " + user.getId());
    }

    @Override
    public void delete(UUID id) {
        // 삭제: ID를 키로 사용하여 맵에서 제거
        data.remove(id);
    }
}