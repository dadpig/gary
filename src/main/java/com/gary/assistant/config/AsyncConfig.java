package com.gary.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Uses Java 21+ Virtual Threads (Project Loom) for async operations.
     * Virtual threads are lightweight, managed by the JVM, and ideal for I/O-bound tasks.
     * Benefits:
     * - Extremely low memory footprint (~1KB per thread vs ~1MB for platform threads)
     * - No need for thread pool sizing - can create millions of virtual threads
     * - Better resource utilization for I/O-bound operations like web scraping
     * - Simplified concurrency model
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
