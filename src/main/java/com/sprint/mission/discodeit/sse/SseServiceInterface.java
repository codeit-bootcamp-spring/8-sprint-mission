package com.sprint.mission.discodeit.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Collection;
import java.util.UUID;

public interface SseServiceInterface {

  /**
   * 클라이언트가 처음 연결을 요청할 때 사용
   *
   * @param receiverId  수신자 ID
   * @param lastEventId 마지막으로 수신한 이벤트 ID (유실 복구용)
   * @return 생성된 SseEmitter 객체
   */
  SseEmitter connect(UUID receiverId, UUID lastEventId);

  /**
   * 특정 사용자들에게 이벤트를 전송할 때 사용
   *
   * @param receiverIds 수신자 ID 목록
   * @param eventName   이벤트 이름
   * @param data        전송할 데이터
   */
  void send(Collection<UUID> receiverIds, String eventName, Object data);

  /**
   * 모든 사용자에게 이벤트를 전송할 때 사용 (공지사항 등)
   *
   * @param eventName 이벤트 이름
   * @param data      전송할 데이터
   */
  void broadcast(String eventName, Object data);
  //클린업, 핑은 구현체에
}