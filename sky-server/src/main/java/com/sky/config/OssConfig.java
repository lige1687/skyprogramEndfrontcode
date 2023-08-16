package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//配置阿里云的 oss对象属性, 创建 Oss utils 对象
@Configuration
@Slf4j
public class OssConfig {
    // 我们已经读入到了propertites 中去了
    // 所以这里第三方bean通过 形式参数直接注入即可
    // 第三方bean , 配置 类一般需要使用 Bean 创建第三方的bean对象
    @Bean
    @ConditionalOnMissingBean // 保证整个 环境 , 该工具类只有一个 , 因为是工具类 , 没有必要有多个
    public AliOssUtil aliOssUtil (AliOssProperties properties) {
        log.info("aliyun工具类Utils在创建 {}", properties);
        AliOssUtil aliOssUtil = new AliOssUtil(properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName());
    return aliOssUtil ;
    }
    }
