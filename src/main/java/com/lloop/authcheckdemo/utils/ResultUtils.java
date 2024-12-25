package com.lloop.authcheckdemo.utils;

import com.lloop.authcheckdemo.common.BaseResponse;
import com.lloop.authcheckdemo.common.ErrorCode;

/**
 * @Author lloop
 * @Create 2024/12/24 23:20
 */
public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(0, "ok", data);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<T>(errorCode);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, T data) {
        return new BaseResponse<T>(errorCode.getCode(), message, data);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, T data) {
        return new BaseResponse<T>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<T>(code, message, null);
    }

}
