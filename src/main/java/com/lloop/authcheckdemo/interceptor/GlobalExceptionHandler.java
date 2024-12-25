package com.lloop.authcheckdemo.interceptor;

import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.BusinessException;
import com.lloop.authcheckdemo.common.ErrorCode;
import com.lloop.authcheckdemo.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author lloop
 * @Create 2024/12/25 14:00
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    // 捕获系统异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e){
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
    }

}
