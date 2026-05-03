package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("AsyncThread-");

    executor.setTaskDecorator(runnable -> {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();
      SecurityContext originalContext = SecurityContextHolder.getContext();
      Authentication authentication = originalContext.getAuthentication();

      return () -> {
        try {
          if (contextMap != null) {
            MDC.setContextMap(contextMap);
          }
          SecurityContext newContext = SecurityContextHolder.createEmptyContext();
          newContext.setAuthentication(authentication);
          SecurityContextHolder.setContext(newContext);

          runnable.run();
        } finally {
          SecurityContextHolder.clearContext();
          MDC.clear();
        }
      };
    });
    executor.initialize();
    return executor;
  }
}
