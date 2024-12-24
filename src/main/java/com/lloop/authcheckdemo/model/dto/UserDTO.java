package com.lloop.authcheckdemo.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author lloop
 * @Create 2024/12/24 21:51
 */
@Data
public class UserDTO {
        /**
         * id
         */
        private Long id;

        /**
         * 用户昵称
         */
        private String username;

        /**
         * 账号
         */
        private String useraccount;

        /**
         * 用户头像
         */
        private String avatarurl;
}
