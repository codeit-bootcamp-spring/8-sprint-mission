# PR 설명: InMemoryJwtRegistry → RedisJwtRegistry (분산 환경)

## InMemoryJwtRegistry의 한계 (분산 환경)

1. **프로세스 로컬 상태**  
   JWT 세션 큐가 JVM 힙의 `ConcurrentHashMap`에만 존재합니다. 동일 사용자가 **로드밸런서 뒤의 서로 다른 인스턴스**로 API/WebSocket 요청을 내면, 로그인·검증·로그아웃이 다른 노드에서 처리될 때 **세션 유효성이 노드마다 달라질 수** 있습니다.

2. **수평 확장 시 일관성 부재**  
   인스턴스 A에서 발급·등록된 토큰이 인스턴스 B의 레지스트리에는 없어, `hasActiveJwtInformationByAccessToken` 등이 **거짓 음성(false negative)**을 내거나, 반대로 한쪽에서만 무효화되어 **보안 정책이 어긋날 수** 있습니다.

3. **재시작 시 전부 소실**  
   애플리케이션 재배포·크래시·스케일 인 시 메모리가 비워지면 **모든 활성 세션 정보가 사라집니다**. Redis 대비 운영 복구·세션 연속성 측면에서 불리합니다.

4. **동시성과 “한 사용자 큐”의 경계**  
   `ConcurrentHashMap`/`ConcurrentLinkedQueue`는 **단일 JVM 내** 동시성에는 적합하지만, **여러 JVM이 같은 논리 큐를 공유**하지는 못합니다. 분산 락 없이 여러 노드가 같은 사용자에 대해 동시에 등록하면, `maxActiveJwtCount` 같은 정책을 **클러스터 전체에서 보장하기 어렵**습니다.

## 본 PR에서 한 일

- **`RedisJwtRegistry`**: 사용자별 JWT 리스트를 Redis List(`discodeit:jwt:user:{userId}`)에 저장하고, 액세스/리프레시 토큰 역조회용 보조 키(`discodeit:jwt:at:*`, `discodeit:jwt:rt:*`)와 TTL을 둡니다. 사용자 단위 변경 시 **`RedisLockProvider`**로 짧은 분산 락을 잡아 동시 갱신을 완화했습니다.
- **`RedisConfig`**: 과제에서 제시한 `RedisTemplate<String, Object>` + `GenericJackson2JsonRedisSerializer`(`@Bean("redisSerializer")`) 구성을 반영했습니다.
- **`RedisLockProvider`**: `com.sprint.mission.discodeit.redis` 패키지에 추가했습니다.
- **`JwtInformation`**: Redis JSON 역직렬화를 위해 `@JsonCreator` / `@JsonProperty` 생성자를 사용합니다.
- **`InMemoryJwtRegistry`**: `RedisJwtRegistry` 빈이 없을 때만 등록(`@ConditionalOnMissingBean(RedisJwtRegistry.class)`). Redis가 없는 테스트 프로파일에서는 InMemory가 사용됩니다.
- **`application-test.yaml`**: Redis 자동구성을 제외하고 캐시를 `simple`로 두어, 테스트에서 Redis 없이 컨텍스트가 뜨도록 했습니다.

## 운영 시 유의

- Redis 장애 시 JWT 레지스트리 동작이 함께 영향을 받으므로, **Redis 가용성·모니터링**이 필요합니다.
- `RedisLockProvider`는 단순 `SETNX` + TTL 기반이라, 장애 시나리오에 따라 **락 해제/연장 전략**을 강화할 여지가 있습니다.
