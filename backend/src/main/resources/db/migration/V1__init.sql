-- =========================================
-- CodeInsight AI V0.1 数据库初始化脚本
-- =========================================

CREATE DATABASE IF NOT EXISTS codeinsight
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE codeinsight;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL UNIQUE,
    `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密',
    `email`       VARCHAR(100),
    `avatar_url`  VARCHAR(255),
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 仓库表
CREATE TABLE IF NOT EXISTS `repository` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `url`         VARCHAR(255) NOT NULL,
    `branch`      VARCHAR(50)  DEFAULT 'main',
    `local_path`  VARCHAR(255) COMMENT '本地存储路径',
    `status`      VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING / CLONING / READY / ERROR',
    `error_msg`   VARCHAR(500),
    `file_count`  INT          DEFAULT 0,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 评审记录表
CREATE TABLE IF NOT EXISTS `review` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `repository_id`   BIGINT       NOT NULL,
    `user_id`         BIGINT       NOT NULL,
    `status`          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING / ANALYZING / COMPLETED / ERROR',
    `summary`         TEXT         COMMENT '项目摘要',
    `report_markdown` MEDIUMTEXT   COMMENT '完整 Markdown 报告',
    `ai_model`        VARCHAR(50)  COMMENT '使用的 AI 模型',
    `token_used`      INT          DEFAULT 0 COMMENT '消耗的 Token 数',
    `error_msg`       VARCHAR(500),
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `completed_at`    DATETIME,
    FOREIGN KEY (`repository_id`) REFERENCES `repository`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_repository_id` (`repository_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审记录表';
