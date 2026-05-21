package com.sprint.mission.discodeit.repository;

import java.util.Set;
import java.util.UUID;

/**
 * SSE 저장 이벤트 (Last-Event-ID 복원용).
 *
 * @param broadcast true 이면 모든 수신자에게 전달된 이벤트
 * @param receiverIds broadcast 가 false 일 때만 사용되는 대상 수신자 집합
 */
public record SseMessage(
		UUID id,
		String eventName,
		String dataJson,
		boolean broadcast,
		Set<UUID> receiverIds
) {

}
