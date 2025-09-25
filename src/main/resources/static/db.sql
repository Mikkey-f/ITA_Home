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

-- 用户oj表
CREATE TABLE IF NOT EXISTS `user_oj`(
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user表',
                                      `luogu_username` VARCHAR(50) NULL COMMENT '洛谷平台用户名',
                                      `leetcode_cn_username` VARCHAR(50) NULL COMMENT 'LeetCode中国站用户名',
                                      `nowcoder_user_id` VARCHAR(50) NULL COMMENT '牛客网用户ID',
                                      `codeforce_username` VARCHAR(50) NULL COMMENT 'Codeforces用户名',
                                      `total_ac_num` INT NULL COMMENT '四个平台ac数之和',
                                      `total_commit_num` INT NULL COMMENT '四个平台commit数之和',
                                      `last_access_time` DATETIME NULL COMMENT '最后访问时间',
                                      `cache_time` DATETIME NULL COMMENT '数据缓存时间',
                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE INDEX `uk_user_id` (`user_id`), -- 确保每个用户只有一条记录
                                      INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户OJ平台账号表';