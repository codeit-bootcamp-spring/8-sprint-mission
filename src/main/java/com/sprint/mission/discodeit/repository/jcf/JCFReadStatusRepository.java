package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> data = new HashMap<>();


    @Override
    public ReadStatus save(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    // 한 유저는 여러 채널을 읽을 수 있다.
    // 하나의 유저 id로 접속한 여러개의 채널 조회
    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        List<ReadStatus> result = new ArrayList<>();
        for (ReadStatus readStatus : data.values()) {
            if (Objects.equals(readStatus.getUserId(), userId)) {
                result.add(readStatus);
            }
        }
        return result;
    }

    // 해당 채널에 참여한 모든 유저들의 접속 정보(ReadStatus)를 한 번에 리스트로 조회해온다.
    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        List<ReadStatus> result = new ArrayList<>();
        for (ReadStatus readStatus : data.values()) {
            if (Objects.equals(readStatus.getChannelId(), channelId)) {
                result.add(readStatus);
            }
        }
        return result;
    }

    // 해당 유저가 이 채널을 읽은 적 있는가, 있다면 그 기록을 가져온다.
    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.values()
                .stream()
                .filter(readStatus -> Objects.equals(readStatus.getUserId(), userId)
                        && Objects.equals(readStatus.getChannelId(), channelId))
                .findFirst();
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        data.values().removeIf(readStatus -> Objects.equals(readStatus.getChannelId(), channelId));
    }
}
