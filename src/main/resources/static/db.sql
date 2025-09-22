-- 创建 ita_home 数据库
CREATE DATABASE IF NOT EXISTS ita_home
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用 ita_home 数据库
USE ita_home;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，建议使用BCrypt加密',
    `avatar` TINYINT DEFAULT 1 CHECK (avatar >= 1 AND avatar <= 9) COMMENT '头像编号，1-9对应9张不同的头像图片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    INDEX `idx_name` (`name`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';