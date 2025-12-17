package com.mxxdone.miniproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 처리 활성화
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        // 가상 스레드는 무한대 생성 가능 -> Pool 설정 불필요
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("Async-VT-");
        executor.setVirtualThreads(true); // 가상 스레드 사용
        return executor;
    }
}