package com.sprint.mission.discodeit.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Bean(name = "taskExecutor")
  public ThreadPoolTaskExecutor taskExecutor(
      @Value("${async.executors.core-size}") int core,
      @Value("${async.executors.max-size}") int max,
      @Value("${async.executors.queue-capacity}") int queue,
      @Value("${async.executors.keep-alive-second}") int keepAlive
  ) {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

    exec.setCorePoolSize(core);
    exec.setMaxPoolSize(max);
    exec.setQueueCapacity(queue);
    exec.setKeepAliveSeconds(keepAlive);
    // 스레드 풀이 포화일 때 호출 스레드에서 작업을 대신 실행
    exec.setRejectedExecutionHandler(new CallerRunsPolicy());
    // 애플리케이션 종료 시 진행 중인 작업을 최대한 정리
    exec.setWaitForTasksToCompleteOnShutdown(true);
    exec.setAwaitTerminationSeconds(20);
    // decorator 설정
    exec.setTaskDecorator(mdcTaskDecorator());
    exec.initialize();

    return exec;
  }

  @Bean
  public TaskDecorator mdcTaskDecorator() {
    return runnable -> {
      Map<String, String> mdcContext = MDC.getCopyOfContextMap();
      SecurityContext securityContext = SecurityContextHolder.getContext();

      return () -> {
        try {
          SecurityContextHolder.setContext(securityContext);

          if (mdcContext != null) {
            MDC.setContextMap(mdcContext);
          }

          runnable.run();
        } finally {
          SecurityContextHolder.clearContext();
          MDC.clear();
        }
      };
    };
  }

  // 이벤트 리스너의 try-catch문에서 잡지 못한 예외 또는 예상치 못한 예외를 처리하는 핸들러
  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {
      log.error("비동기 메서드 실행 중 예외 발생! 메서드명: {}, 메시지: {}",
          method.getName(), ex.getMessage());
      for (Object param : params) {
        log.error("파라미터: {}", param);
      }
    };
  }
}
