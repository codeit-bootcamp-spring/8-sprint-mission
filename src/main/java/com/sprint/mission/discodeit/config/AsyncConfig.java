package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.handler.CustomAsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    private ThreadPoolTaskExecutor buildExecutor(int core, int max, int queue, int keepAlive, String prefix) {

        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        // 스레드 풀의 세부 스펙을 설정한다.
        exec.setCorePoolSize(core);
        exec.setMaxPoolSize(max);
        exec.setQueueCapacity(queue);
        exec.setKeepAliveSeconds(keepAlive);
        exec.setThreadNamePrefix(prefix + "-");
        // 스레드 풀이 포화일 때 호출 스레드에서 작업을 실행하여 백프레셔(원만한 저하)를 유도한다.
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 애플리케이션 종료 시 진행 중 작업을 최대한 정리
        exec.setWaitForTasksToCompleteOnShutdown(true);
        // 애플리케이션 종료 시 진행 중 작업을 최대한 정리
        exec.setAwaitTerminationSeconds(20);
        // 로깅 MDC/보안 컨텍스트 전파
        exec.setTaskDecorator(new CompositeTaskDecorator());
        // 스레드 풀 초기화
        exec.initialize();

        // 설정된 스레드 풀을 반환
        return exec;
    }

    @Bean(name = "notificationTaskExecutor")
    public ThreadPoolTaskExecutor notificationTaskExecutor() {
        return buildExecutor(4, 8, 500, 60, "noti-task-");
    }
}
