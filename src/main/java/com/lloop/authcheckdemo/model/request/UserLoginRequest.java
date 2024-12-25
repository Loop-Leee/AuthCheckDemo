package com.lloop.authcheckdemo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author lloop
 * @Create 2024/12/25 14:34
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String account;

    private String userPassword;

}
