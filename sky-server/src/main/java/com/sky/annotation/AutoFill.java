package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解, 用于公共字段填充的 标识处理, 减少重复的代码
 */
@Target(ElementType.METHOD) // 指定该注解加到方法上
@Retention(RetentionPolicy.RUNTIME)// 固定的
public @interface AutoFill {
        // 数据库操作类型 , 定义了 insert和update , 来表示操作
    OperationType value(); // 定义 注解的value 是哪些
}
