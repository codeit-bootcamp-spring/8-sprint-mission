package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 비동기 실행 시 MDC(요청 ID 등)와 {@link SecurityContext}를 워커 스레드로 복사합니다.
 */
public class MdcAndSecurityContextTaskDecorator implements TaskDecorator {

  @Override
  @NonNull
  public Runnable decorate(@NonNull Runnable runnable) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();
    SecurityContext original = SecurityContextHolder.getContext();
    SecurityContext securityContextCopy = SecurityContextHolder.createEmptyContext();
    securityContextCopy.setAuthentication(original.getAuthentication());
    return () -> {
      try {
        if (mdcContext != null) {
          MDC.setContextMap(mdcContext);
        } else {
          MDC.clear();
        }
        SecurityContextHolder.setContext(securityContextCopy);
        runnable.run();
      } finally {
        MDC.clear();
        SecurityContextHolder.clearContext();
      }
    };
  }
}
