create database if not exists authcheckdemo;

use authcheckdemo;

create table if not exists authcheckdemo.user
(
    username     varchar(256)                       null comment '用户昵称',
    id           bigint auto_increment comment 'id'
    primary key,
    account      varchar(256)                       null comment '账号',
    avatar_url   varchar(1024)                      null comment '用户头像URL',
    gender       tinyint                            null comment '性别',
    password     varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    user_status  int      default 0                 not null comment '状态 0 - 正常',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete    tinyint  default 0                 not null comment '是否删除',
    role         int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员'
)
    comment '用户';

