package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自定义 任务类
 */
@Component // 代表交给spring容器处理
@Slf4j
public class Mytask {

    
@Scheduled(cron =  "0/5  * * * * ?") // 从第 0秒 开始, 每隔五秒触发一次
// 编写比较复杂的 定时业务逻辑
    public  void  executeTask( )
{
    log.info("执行业务逻辑...");
}
}

