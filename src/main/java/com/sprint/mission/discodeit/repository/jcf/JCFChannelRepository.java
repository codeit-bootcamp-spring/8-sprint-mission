package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFChannelRepository implements ChannelRepository {

  private final Map<UUID, Channel> data;

  public JCFChannelRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
    this.data = new ConcurrentHashMap<>();
  }

  @Override
  public Channel save(Channel channel) {
    this.data.put(channel.getId(), channel);
    return channel;
  }

  @Override
  public Optional<Channel> findById(UUID id) {
    return Optional.ofNullable(this.data.get(id));
  }

  @Override
  public List<Channel> findAll() {
    return this.data.values().stream().toList();
  }

  @Override
  public void delete(UUID id) {
    this.data.remove(id);
  }
}
