package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  public static final String ASYNC_EXECUTOR = "taskExecutor";
  public static final String EVENT_TASK_EXECUTOR = "eventTaskExecutor";

  @Bean(name = ASYNC_EXECUTOR)
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("discodeit-async-");
    executor.setTaskDecorator(new MdcAndSecurityContextTaskDecorator());
    executor.initialize();
    return executor;
  }

  @Bean(name = EVENT_TASK_EXECUTOR)
  public TaskExecutor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("discodeit-event-");
    executor.setTaskDecorator(new MdcAndSecurityContextTaskDecorator());
    executor.initialize();
    return executor;
  }
}
