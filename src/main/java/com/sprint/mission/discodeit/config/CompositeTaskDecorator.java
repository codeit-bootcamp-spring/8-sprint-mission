package com.sprint.mission.discodeit.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class CompositeTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

        // 메인 스레드: 현재 스레드의 데이터를 복사해둔다.
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();

        return () -> {
            try {
                // 비동기 스레드: 복사한 MDC 데이터를 주입
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                SecurityContext newContext = SecurityContextHolder.createEmptyContext();
                newContext.setAuthentication(auth);
                // 비동기 스레드: 복사한 SecurityContext를 주입한다.
                SecurityContextHolder.setContext(newContext);

                // 실제 비동기 로직 수행
                runnable.run();
            } finally {
                // 스레드 반납 전 컨텍스트를 클리어 (스레드 오염 방지)
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
