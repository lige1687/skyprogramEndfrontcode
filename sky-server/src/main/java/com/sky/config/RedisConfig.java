package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j

public class RedisConfig {
    @Bean// 在形式参数中,注入 链接工厂对象
        public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory)
        {
            log.info("redistemplate 创建中" );
            // 返回一个redis template 来操作redis数据库
            RedisTemplate redisTemplate = new RedisTemplate();
           // 设置redisTemplate 的 链接工厂
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            // 设置redis key 的序列化器 , 字符串 序列转换器
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return  redisTemplate;
        }
}
