package com.lloop.authcheckdemo.model.dto;

import lombok.Data;

/**
 * @Author lloop
 * @Create 2024/12/26 15:28
 * @Description: 用户Token对象
 */
@Data
public class UserTokenInfo {

    /**
     * ID，唯一
     */
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户昵称
     */
    private String username;

}