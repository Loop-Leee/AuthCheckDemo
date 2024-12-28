package com.lloop.authcheckdemo.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author lloop
 * @Create 2024/12/28 16:11
 */
@Getter
public enum UserRoleEnum {


    ADMIN(1, "管理员"),

    USER(0, "普通用户");

    @EnumValue
    private final Integer value;

    private final String description;

    UserRoleEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

}
