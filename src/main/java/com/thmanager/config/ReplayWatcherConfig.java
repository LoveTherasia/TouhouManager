package com.thmanager.config;

import com.thmanager.service.ReplayWatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Replay监控服务配置类
 * 在应用启动时自动启动Replay文件夹监控服务
 */
@Configuration
public class ReplayWatcherConfig {

    private final ReplayWatcherService replayWatcherService;

    @Autowired
    public ReplayWatcherConfig(ReplayWatcherService replayWatcherService) {
        this.replayWatcherService = replayWatcherService;
    }

    /**
     * 应用启动完成后触发
     */
    @EventListener(ContextRefreshedEvent.class)
    public void startReplayWatcher() {
        // 启动Replay文件夹监控服务
        replayWatcherService.startWatching();
    }
}
