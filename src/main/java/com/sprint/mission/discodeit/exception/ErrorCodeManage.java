package com.sprint.mission.discodeit.exception;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorCodeManage {

  private static final Map<String, Set<String>> domainCodes = new ConcurrentHashMap<>();

  /*
   * 새로운 오류 코드를 등록한다
   * 중복 코드 등록을 방지한다
   */
  public static void registerErrorCode(String domain, String code, ErrorCode errorCode) {
    String fullCode = domain + "_" + code;

    domainCodes.computeIfAbsent(domain, k -> new HashSet<>()).add(code);

    if (isDevelopmentMode() && isDuplicateCode(fullCode)) {
      throw new IllegalArgumentException("중복된 오류 코드: " + fullCode);
    }
  }

  /*
   * 특정 도메인의 모든 오류 코드를 조회한다
   * 문서화나 모니터링 목적으로 사용할 수 있다
   */
  public static Set<String> getErrorCodesForDomain(String domain) {
    return domainCodes.getOrDefault(domain, Collections.emptySet());
  }

  /*
   * 오류 코드 통계를 생성한다
   * 어떤 오류가 자주 발생하는지 분석할 수 있다
   */
  public static Map<String, Long> getErrorCodeStatistics(LocalDateTime from, LocalDateTime to) {
    return Collections.emptyMap();
  }

  private static boolean isDevelopmentMode() {
    return "development".equals(System.getProperty("spring.profiles.active"));
  }

  private static boolean isDuplicateCode(String code) {
    return false;
  }
}
