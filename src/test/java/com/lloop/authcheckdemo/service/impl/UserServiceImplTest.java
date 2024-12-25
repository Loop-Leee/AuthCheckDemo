package com.lloop.authcheckdemo.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author lloop
 * @Create 2024/12/25 21:40
 */
class UserServiceImplTest {


    @Test
    void isValidPassword() {
        String userPassword = "123qweasdA!";
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,12}$";
        boolean matches = userPassword.matches(pattern);
        assertTrue(matches);
    }

}