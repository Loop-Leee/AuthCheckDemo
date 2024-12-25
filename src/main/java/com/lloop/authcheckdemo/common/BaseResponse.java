package com.lloop.authcheckdemo.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author lloop
 * @Create 2024/12/24 23:07
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, String message){
        this.code = code;
        this.message = message;
    }

    public BaseResponse(ErrorCode errorCode){
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

}
