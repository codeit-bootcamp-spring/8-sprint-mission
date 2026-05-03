# PR 설명: 분산 환경에서 WebSocket / SSE 한계와 Kafka 실시간 푸시

## WebSocket·SSE의 한계 (수평 확장)

1. **연결이 특정 JVM에 종속**  
   브라우저의 WebSocket·SSE 세션은 **요청을 처리한 인스턴스의 메모리**(`SimpMessagingTemplate` 대상 세션, `SseEmitterRepository`)에만 붙습니다. 로드밸런서 뒤에 백엔드가 여러 대이면, **다른 인스턴스에서는 그 연결 객체에 접근할 수 없습니다.**

2. **도메인 이벤트와 푸시 경로의 불일치**  
   메시지 생성·알림 등은 한 인스턴스에서 처리되고, 해당 인스턴스에만 STOMP/SSE 클라이언트가 붙어 있으면 **나머지 노드의 사용자는 실시간 갱신을 받지 못합니다.**

3. **재시작·스케일 인 시 연결 단절**  
   인스턴스가 내려가면 그 위의 모든 WebSocket/SSE 연결이 끊기며, **다른 노드로의 무중단 이전**이 구조만으로는 보장되지 않습니다.

4. **(부가) 단일 진실 공급원 부재**  
   푸시 전용 상태가 JVM마다 흩어지면, **관측·재전송·백프레셔**를 일관되게 다루기 어렵습니다.

## 본 PR에서 한 일

- **토픽** `discodeit.RealtimePushEvent`에 WS 전송·SSE 타깃·SSE 브로드캐스트를 담는 `RealtimePushEnvelope` JSON을 발행합니다.
- **`RealtimePushEventPublisher`**: `WebSocketRequiredEventListener`와 `SseService`가 Kafka로 푸시를 위임합니다.
- **`RealtimePushKafkaListener`**: 수신 후 **로컬**에서만 `SimpMessagingTemplate.convertAndSend` 및 `SseService.deliverToLocalReceivers` / `deliverBroadcastToLocalEmitters`를 호출합니다.
- **Consumer group**: 도메인 처리용 `spring.kafka.consumer.group-id`(`discodeit-group`)와 **분리**하여, `discodeit.kafka.realtime-push-group-id`를 사용합니다.  
  - **인스턴스마다 이벤트를 모두 받으려면** Kafka에서 **그룹마다 오프셋이 분리**되므로, **노드별로 유일한 group id**가 필요합니다.  
  - 기본값은 `discodeit-realtime-push-${spring.application.name}-${random.uuid}`로 **JVM 기동 시마다** 고유 그룹을 부여하고, `auto-offset-reset: latest`로 **기동 이후** 메시지만 소비합니다.  
  - 운영(Kubernetes 등)에서는 `DISCODEIT_KAFKA_REALTIME_PUSH_GROUP_ID`로 **Pod 이름 등 안정적인 식별자**를 주는 것을 권장합니다.
- **테스트**: `KafkaAutoConfiguration` 제외 시 `KafkaTemplate`이 없어 기존 Kafka 리스너·발행기·실시간 푸시 빈이 스킵되고, WS는 로컬 `SimpMessagingTemplate`, SSE는 기존처럼 **인메모리 직접 전달**로 동작합니다.

## 운영 시 참고

- 실시간 토픽은 **멱등·순서** 요구가 있으면 파티션 키·토픽 분리 설계를 검토하세요.
- `latest` 오프셋은 **배포 직후 짧은 공백**이 생길 수 있습니다. 필요 시 운영 전용 그룹 id와 오프셋 정책을 조정하세요.
