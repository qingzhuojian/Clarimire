-- =========================================
-- Clarimire 数据库初始化脚本
-- 清浊鉴 - 水污染与智能预警系统
-- =========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS clarimire DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE clarimire;

-- =========================================
-- 用户表
-- =========================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：admin/user',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '58e894133ce4454022adbdb0619158e6', '系统管理员', 'admin');

-- =========================================
-- 水库基础信息表
-- =========================================
CREATE TABLE IF NOT EXISTS `water_reservoir` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reservoir_name` VARCHAR(100) NOT NULL COMMENT '水库名称',
    `short_name` VARCHAR(32) DEFAULT NULL COMMENT '简称',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '位置',
    `capacity` DECIMAL(12,2) DEFAULT NULL COMMENT '总库容(万立方米)',
    `flood_level` DECIMAL(8,2) DEFAULT NULL COMMENT '汛限水位(米)',
    `max_level` DECIMAL(8,2) DEFAULT NULL COMMENT '历史最高水位(米)',
    `avg_level` DECIMAL(8,2) DEFAULT NULL COMMENT '多年平均水位(米)',
    `normal_level` DECIMAL(8,2) DEFAULT NULL COMMENT '正常蓄水位(米)',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '水库类型',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reservoir_name` (`reservoir_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水库基础信息表';

-- =========================================
-- 水情数据表
-- =========================================
CREATE TABLE IF NOT EXISTS `water_situation` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reservoir_name` VARCHAR(100) NOT NULL COMMENT '库名',
    `date` DATETIME NOT NULL COMMENT '日期时间',
    `water_level` DECIMAL(8,2) DEFAULT NULL COMMENT '库水位(米)',
    `storage` DECIMAL(10,2) DEFAULT NULL COMMENT '蓄水量(万立方米)',
    `avg_inflow` DECIMAL(8,2) DEFAULT NULL COMMENT '日平均入库流量(立方米/秒)',
    `avg_outflow` DECIMAL(8,2) DEFAULT NULL COMMENT '日平均出库流量(立方米/秒)',
    `yoy_increase` DECIMAL(10,2) DEFAULT NULL COMMENT '比去年同期增减(万立方米)',
    `total_capacity` DECIMAL(10,2) DEFAULT NULL COMMENT '总库容(万立方米)',
    `flood_level` DECIMAL(8,2) DEFAULT NULL COMMENT '汛限水位(米)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_reservoir_name` (`reservoir_name`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水情数据表';

-- 水情数据由 sql/seed/import_excel.py 从 Excel 导入

-- =========================================
-- 监测断面数据表
-- =========================================
CREATE TABLE IF NOT EXISTS `section_monitor` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `monitor_point_name` VARCHAR(64) NOT NULL COMMENT '监测点名称',
    `reservoir_name` VARCHAR(64) NOT NULL COMMENT '水库名称',
    `year` INT NOT NULL COMMENT '年份',
    `month` INT NOT NULL COMMENT '月份',
    `oxygen` DECIMAL(8,3) DEFAULT NULL COMMENT '溶解氧(mg/L)',
    `potassium_permanganate` DECIMAL(8,3) DEFAULT NULL COMMENT '高锰酸盐指数(mg/L)',
    `cod` DECIMAL(8,3) DEFAULT NULL COMMENT '化学需氧量(mg/L)',
    `flow` DECIMAL(8,3) DEFAULT NULL COMMENT '流量(m³/s)',
    `water_depth` DECIMAL(8,3) DEFAULT NULL COMMENT '水深(m)',
    `total_nitrogen` DECIMAL(8,3) DEFAULT NULL COMMENT '总氮(mg/L)',
    `total_phosphorus` DECIMAL(8,3) DEFAULT NULL COMMENT '总磷(mg/L)',
    `ammonia_nitrogen` DECIMAL(8,3) DEFAULT NULL COMMENT '氨氮(mg/L)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_monitor_point` (`monitor_point_name`),
    KEY `idx_reservoir` (`reservoir_name`),
    KEY `idx_year_month` (`year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测断面数据表';

-- 监测断面数据由 sql/seed/import_excel.py 从 Excel 导入

-- =========================================
-- 操作日志表
-- =========================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
    `detail` TEXT DEFAULT NULL COMMENT '操作详情',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
