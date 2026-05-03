package com.sprint.mission.discodeit.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableAsync
@EnableRetry
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  private ThreadPoolTaskExecutor buildExecutor(int core, int max, int queue, int keepAlive,
      String prefix) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(core);
    executor.setMaxPoolSize(max);
    executor.setQueueCapacity(queue);
    executor.setKeepAliveSeconds(keepAlive);
    executor.setThreadNamePrefix(prefix + "-");

    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(20);
    executor.setTaskDecorator(new ContextCopyingDecorator());
    executor.initialize();
    return executor;
  }

  @Bean(name = "eventTaskExecutor")
  public ThreadPoolTaskExecutor eventTaskExecutor() {
    return buildExecutor(2, 10, 50, 60, "event-task");
  }

  private class ContextCopyingDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

      Map<String, String> mdcContext = MDC.getCopyOfContextMap();
      SecurityContext parentContext = SecurityContextHolder.getContext();
      Authentication auth = parentContext.getAuthentication();

      return () -> {
        try {
          if (mdcContext != null) {
            MDC.setContextMap(mdcContext);
          }
          SecurityContext childContext = SecurityContextHolder.createEmptyContext();
          childContext.setAuthentication(auth);
          SecurityContextHolder.setContext(childContext);
          runnable.run();
        } finally {
          MDC.clear();
          SecurityContextHolder.clearContext();
        }
      };
    }
  }

}
