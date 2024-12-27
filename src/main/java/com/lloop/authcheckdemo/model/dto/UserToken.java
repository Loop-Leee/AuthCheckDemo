package com.lloop.authcheckdemo.model.dto;

import lombok.Data;

/**
 * @Author lloop
 * @Create 2024/12/26 15:28
 * @Description: 用户Token对象
 */
@Data
public class UserToken extends UserTokenInfo {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

}