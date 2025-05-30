package com.lloop.authcheckdemo.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author lloop
 * @Create 2024/12/24 23:15
 */

@Getter
@ToString
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误码
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    LOGIN_EXPIRED(40102, "登录过期", ""),
    NO_AUTH(40101, "无权限", ""),
    FORBIDDEN(40301, "禁止操作", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    ;

    private final int code;

    private final String message;

    private final String description;


}
