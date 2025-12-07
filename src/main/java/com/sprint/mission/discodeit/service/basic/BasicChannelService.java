package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BasicChannelService implements ChannelService {

    // Repository 인터페이스 필드 선언 (저장소 구현체에 의존하지 않음)
    private final ChannelRepository channelRepository;

    // 생성자를 통해 Repository 인터페이스를 주입받아 초기화합니다.
    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    // BasicChannelService는 특별한 비즈니스 규칙이 없으므로,
    // 모든 CRUD 기능을 Repository에 위임합니다.

    @Override
    public Channel save(Channel channel) {
        // 저장 로직은 Repository에 위임
        return channelRepository.save(channel);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        // 조회 로직은 Repository에 위임
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        // 전체 조회 로직은 Repository에 위임
        return channelRepository.findAll();
    }

    @Override
    public Channel update(Channel channel) {
        // 수정 로직은 Repository에 위임
        // (Channel 객체 내부에서 update() 메서드가 호출되어 상태가 변경된 후 넘어왔다고 가정)
        return channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        // 삭제 로직은 Repository에 위임
        channelRepository.delete(id);
    }
}