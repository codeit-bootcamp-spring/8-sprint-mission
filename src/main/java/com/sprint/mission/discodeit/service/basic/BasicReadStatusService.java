package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.cache.CacheNames;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

		private final ReadStatusRepository readStatusRepository;
		private final UserRepository userRepository;
		private final ChannelRepository channelRepository;
		private final ReadStatusMapper readStatusMapper;
		private final CacheManager cacheManager;

		@Transactional
		@Override
		@CacheEvict(cacheNames = CacheNames.CHANNELS_BY_USER, key = "#request.userId()")
		public ReadStatusDto create(ReadStatusCreateRequest request) {
				UUID userId = request.userId();
				UUID channelId = request.channelId();

				User user = userRepository.findById(userId)
						.orElseThrow(
								() -> new NoSuchElementException("User with id " + userId + " does not exist"));
				Channel channel = channelRepository.findById(channelId)
						.orElseThrow(
								() -> new NoSuchElementException("Channel with id " + channelId + " does not exist")
						);

				ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(user.getId(),
								channel.getId())
						.orElse(null);

				if (readStatus != null) {
						throw DuplicateReadStatusException.forUserIdAndChannelId(user.getId(), channel.getId());
				}

				readStatus = readStatusRepository.save(new ReadStatus(user, channel, request.lastReadAt()));
				return readStatusMapper.toDto(readStatus);
		}

		@Override
		public ReadStatusDto find(UUID readStatusId) {
				return readStatusRepository.findById(readStatusId)
						.map(readStatusMapper::toDto)
						.orElseThrow(
								() -> new NoSuchElementException(
										"ReadStatus with id " + readStatusId + " not found"));
		}

		@Override
		public List<ReadStatusDto> findAllByUserId(UUID userId) {
				return readStatusRepository.findAllByUserId(userId).stream()
						.map(readStatusMapper::toDto)
						.toList();
		}

		@Transactional
		@Override
		public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
				ReadStatus readStatus = readStatusRepository.findById(readStatusId)
						.orElseThrow(
								() -> new NoSuchElementException(
										"ReadStatus with id " + readStatusId + " not found"));
				readStatus.update(request.newLastReadAt(), request.newNotificationEnabled());
				return readStatusMapper.toDto(readStatus);
		}

		@Transactional
		@Override
		public void delete(UUID readStatusId) {
				ReadStatus readStatus = readStatusRepository.findById(readStatusId)
						.orElseThrow(
								() -> new NoSuchElementException(
										"ReadStatus with id " + readStatusId + " not found"));
				UUID userId = readStatus.getUser().getId();
				readStatusRepository.delete(readStatus);
				var cache = cacheManager.getCache(CacheNames.CHANNELS_BY_USER);
				if (cache != null) {
						cache.evict(userId);
				}
		}
}
