package com.thmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 东方Project游戏管理器的主应用程序入口类
 * 
 * 这是一个Spring Boot应用程序，用于管理东方Project系列游戏的：
 * - 游戏安装和启动
 * - Replay文件的扫描、解析和管理
 * - 游戏数据统计和分析
 * 
 * @SpringBootApplication 注解表示这是一个Spring Boot应用程序，
 *                        它会自动配置Spring框架并扫描当前包及其子包下的组件。
 */
@SpringBootApplication
public class ThManagerApplication {

    /**
     * 应用程序的主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ThManagerApplication.class, args);
    }
}
