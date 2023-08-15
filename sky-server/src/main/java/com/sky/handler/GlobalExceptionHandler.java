package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    @ExceptionHandler
    // 将捕捉的异常写到形参中
    public  Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // 报错信息 给搞过来处理一下
        // Duplicate entry "lige" for key 'employee idx_username'
        String message = ex.getMessage();
        if( message.contains("Duplicate entry"))
        {
            String[] split = message.split(""); // 使用javaString 进行字符串的分割
            //得到我们想要的用户名
            String username = split[3] ;
            // 已经存在 这个常量, 也通过 常量定义了, 优雅, 反正不能写死
            String msg= username + MessageConstant.ALREADY_EXISTED;
            return Result.error(msg);
        }
        else {
                return Result.error(MessageConstant.UNKNOWN_ERROR) ;
                // 否则就是发生了未知的异常
        }

    }
}
