package com.lloop.authcheckdemo.common;

import com.lloop.authcheckdemo.model.dto.UserTokenInfo;

/**
 * @Author lloop
 * @Create 2024/12/24 21:57
 */
public class UserHolder {

    private static final ThreadLocal<UserTokenInfo> currUser = new ThreadLocal<UserTokenInfo>();

    public static void saveUser(UserTokenInfo userTokenInfo){
        currUser.set(userTokenInfo);
    }

    public static UserTokenInfo getUser(){
        return currUser.get();
    }

    public static void removeUser(){
        currUser.remove();
    }

}
