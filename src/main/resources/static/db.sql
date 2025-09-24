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
    `mail` VARCHAR(50) NOT NULL UNIQUE COMMENT '邮箱，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，建议使用BCrypt加密',
    `avatar` TINYINT DEFAULT 1 CHECK (avatar >= 1 AND avatar <= 9) COMMENT '头像编号，1-9对应9张不同的头像图片',
    `group_id` TINYINT CHECK (group_id >= 1 AND group_id <= 3) COMMENT '分组id,1-前端，2-java后端，3-cpp后端',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    INDEX `idx_name` (`name`),
    INDEX `idx_email` (`mail`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 创建竞赛信息表
CREATE TABLE IF NOT EXISTS `competition` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '竞赛ID，主键',
    `title` VARCHAR(20) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT'内容',
    `picture` VARCHAR(255) NOT NULL COMMENT '图片',
    INDEX `idx_title` (`title`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛信息表';

-- 创建成员表
CREATE TABLE IF NOT EXISTS `member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID，主键',
    `name` VARCHAR(50) NOT NULL COMMENT '成员名称',
    `content` TEXT COMMENT '内容',
    `picture` VARCHAR(255) COMMENT '图片',
    INDEX `idx_name` (`name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员信息表';