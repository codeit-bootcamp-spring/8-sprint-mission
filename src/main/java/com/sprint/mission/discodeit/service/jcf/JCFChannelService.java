package com.sprint.mission.discodeit.service.jcf; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.NoSuchElementException; // 예외 처리용 import

public class JCFChannelService implements ChannelService {

    // 1. JCF (Map) 필드 선언 (메모리 저장소)
    private final Map<UUID, Channel> data;

    // 2. 생성자
    public JCFChannelService() {
        this.data = new HashMap<>();
    }

    // --- Service 인터페이스 구현 (update 메서드 추가) ---

    @Override
    public Channel create(String name, UUID ownerId) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(name, "채널 이름");
        if (ownerId == null) {
            throw new IllegalArgumentException("소유자 ID는 필수입니다.");
        }

        // 2. Entity 객체 생성
        Channel newChannel = new Channel(name, ownerId);

        // 3. JCF Map에 저장
        data.put(newChannel.getId(), newChannel);
        return newChannel;
    }

    @Override
    public Channel update(UUID channelId, String newName, UUID newOwnerId) {
        //  1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(newName, "새 채널 이름");
        if (newOwnerId == null) {
            throw new IllegalArgumentException("새 소유자 ID는 필수입니다.");
        }

        // 2. 대상 Entity를 JCF Map에서 조회
        Channel channelToUpdate = Optional.ofNullable(data.get(channelId))
                .orElseThrow(() -> new NoSuchElementException("수정할 채널 ID를 찾을 수 없습니다: " + channelId));

        // 3. Entity의 상태 변경 메서드 호출 (Channel.java에 update 메서드가 있어야 함)
        channelToUpdate.update(newName, newOwnerId);

        // 4. JCF Map에 다시 저장 (Map에 이미 ID가 존재하므로 덮어쓰기 됨)
        // data.put(channelToUpdate.getId(), channelToUpdate); // update()가 참조형을 수정했으므로 생략 가능하나 명시적으로 저장
        return channelToUpdate;
    }

    // 나머지 findById, findAll, delete 메서드

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Channel> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!data.containsKey(id)) {
            throw new NoSuchElementException("삭제할 채널 ID를 찾을 수 없습니다: " + id);
        }
        data.remove(id);
    }
}