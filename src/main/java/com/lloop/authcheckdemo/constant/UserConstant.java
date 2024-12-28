package com.lloop.authcheckdemo.constant;

/**
 * @Author lloop
 * @Create 2024/12/25 23:05
 */
public class UserConstant {

    // Redis prefix
    public static final String USER_REFRESH_TOKEN_PREFIX = "user:login:refresh:";
    public static final String TOKEN_BLACKLIST_PREFIX = "user:blacklist:";

    // Role
    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_USER = 0;


}
