package com.lloop.authcheckdemo.utils;

import com.lloop.authcheckdemo.model.dto.UserDTO;

/**
 * @Author lloop
 * @Create 2024/12/24 21:57
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> currUser = new ThreadLocal<UserDTO>();

    public static void saveUser(UserDTO userDTO){
        currUser.set(userDTO);
    }

    public static UserDTO getUser(){
        return currUser.get();
    }

    public static void removeUser(){
        currUser.remove();
    }

}
