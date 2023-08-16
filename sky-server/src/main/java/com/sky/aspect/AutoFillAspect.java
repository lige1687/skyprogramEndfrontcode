package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.patterns.AndSignaturePattern;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

// 自定义切面, 通过反射实现公共字段的填充
@Aspect
@Component // Bean
@Slf4j // 记录日志

public class AutoFillAspect {
    /**
     * 切面就是 切入点和 通知组成的
     * 定义切入点 , 对哪些类的哪些方法进行拦截 .
     * 切点表达式也是一个 重要的知识 ,首先是返回类型, 然后是包名,类名, 方法名, 最后是参数 , .. 表示任意参数
     * . 表示任意一个参数 ( 注意掌握区别
     *  同时不是所有的mapper 方法都需要加强, 切点还需满足 有对应的注解!
     */

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autofillPointCut( ) {
        // 定义通知, 这里很明显是前置通知 , 进行公共字段的赋值
    }
        @Before("autofillPointCut()")
        public  void autoFill (JoinPoint joinPoint)
        {
            log.info("开始进行公共字段的填充");

            // 拆解问题 , 为几个步骤 , 关键就是joint point  参数了
            // 获取被 加强方法的,数据库操作类型, insert还是什么 update , insert需要赋值四个属性,  update需要赋值两个属性
            MethodSignature signature = (MethodSignature)joinPoint.getSignature(); // 获取方法签名 ,进行强制转换
            AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取方法的注解对象
            OperationType operationType = autoFill.value(); // 至此得到了 操作类型
            // 获取实体entity  ,通过 joint point , 代表的是被 加强的方法本身 , 也就是反射
            Object[] args = joinPoint.getArgs(); // 获取方法的所有参数, 保证实体 entity放在第一个位置的约定
            // 防止空指针
            if( args.length ==0 || args == null) { return ;}
            Object entity = args[0];// 放心的取第一个参数  , 通过object , 别用enployee , 为了复用性


            LocalDateTime now = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();
            if ( operationType == OperationType.INSERT)
            {
                // 通过反射获取 set 方法, 并且使用try catch处理异常
                try {
                    // 通过方法名, 以及方法参数 ,唯一的锁定一个方法对象
                    // 为了防止 自己手写方法名的时候写错, 将方法名也定义在了 常量类 中 ,优雅

                    Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                    setCreateTime.invoke(entity, now);
                    setCreateUser.invoke(entity, currentId);
                    setUpdateTime.invoke(entity,now);
                    setUpdateUser.invoke(entity, currentId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else  if ( operationType == OperationType.UPDATE)
            {
                try {
                    Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                    setUpdateTime.invoke(entity,now);
                    setUpdateUser.invoke(entity, currentId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }



            }
            // 给公共属性统一赋值 , 创建人, 创建时间, 修改人, 修改时间, 通过thread Local


        }
    }

