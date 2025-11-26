package com.mxxdone.miniproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 처리 활성화
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);    // 기본 스레드 수
        executor.setMaxPoolSize(10);    // 최대 스레드 수
        executor.setQueueCapacity(100); // 대기 큐 사이즈
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
