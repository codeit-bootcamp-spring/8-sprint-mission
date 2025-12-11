package com.sprint.mission.discodeit.service.basic; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository; //  Repository import
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ChannelService 인터페이스 구현
public class BasicChannelService implements ChannelService {

    // 1. Repository 필드 선언 (의존성 주입 대상)
    private final ChannelRepository channelRepository;

    // 2. Repository를 주입받는 생성자 (DI)
    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    // --- Service 인터페이스 구현 (책임 분리 및 유효성 검사 반영) ---

    @Override
    public Channel create(String name, UUID ownerId) {
        // 1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(name, "채널 이름");
        if (ownerId == null) {
            throw new IllegalArgumentException("소유자 ID는 필수입니다.");
        }

        // 2. Entity 객체 생성 (Service 책임)
        Channel newChannel = new Channel(name, ownerId);

        // 3. Repository에 저장 요청
        return channelRepository.save(newChannel);
    }

    @Override
    public Channel update(UUID channelId, String newName, UUID newOwnerId) {
        // 1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(newName, "새 채널 이름");
        if (newOwnerId == null) {
            throw new IllegalArgumentException("새 소유자 ID는 필수입니다.");
        }

        // 2. 대상 Entity를 Repository에서 조회
        Channel channelToUpdate = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 채널을 찾을 수 없습니다: " + channelId));

        // 3. Entity의 상태 변경 메서드 호출
        channelToUpdate.update(newName, newOwnerId);

        // 4. Repository에 수정된 Entity 저장
        return channelRepository.save(channelToUpdate);
    }

    // 나머지 findById, findAll, delete 메서드

    @Override
    public Optional<Channel> findById(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}