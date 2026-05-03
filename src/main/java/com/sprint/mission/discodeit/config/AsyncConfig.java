package com.sprint.mission.discodeit.config;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
public class AsyncConfig {

  // 메서드 이름 변경 가능성 염두해서 빈 이름 명시
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);   // 기본 스레드 수
    executor.setMaxPoolSize(20);    // 최대 스레드 수
    executor.setQueueCapacity(500); // 큐 용량
    executor.setThreadNamePrefix("AsyncThread-");
    executor.setTaskDecorator(new ContextCopyTaskDecorator());
    executor.initialize();
    return executor;
  }

  @Bean(name = "eventTaskExecutor")
  public Executor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("EventAsync-");
    executor.setTaskDecorator(new ContextCopyTaskDecorator());
    executor.initialize();
    return executor;
  }

  /**
   * TaskDecorator: 부모 스레드의 Context를 자식(비동기) 스레드로 복사한다.
   */
  static class ContextCopyTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
      // 부모 스레드의 MDC와 SecurityContext를 캡처
      Map<String, String> contextMap = MDC.getCopyOfContextMap();
      SecurityContext securityContext = SecurityContextHolder.getContext();

      return () -> {
        try {
          // 자식 스레드에 복사
          if (contextMap != null) MDC.setContextMap(contextMap);
          SecurityContextHolder.setContext(securityContext);
          runnable.run();
        } finally {
          // 작업 종료 후 자식 스레드 컨텍스트 클리어
          MDC.clear();
          SecurityContextHolder.clearContext();
        }
      };
    }
  }
}
