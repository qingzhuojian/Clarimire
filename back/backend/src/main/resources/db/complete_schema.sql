-- =====================================================
-- 水环境风险管控系统 - 完整数据库初始化脚本
-- 【重要】此脚本会先删除旧数据库，请确保已备份重要数据
-- =====================================================

-- 关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除数据库（如果存在）
DROP DATABASE IF EXISTS water_data;

-- 重新创建数据库
CREATE DATABASE water_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用新数据库
USE water_data;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(MD5加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `role` ENUM('admin', 'inspector', 'public') DEFAULT 'public' COMMENT '角色',
    `phone` VARCHAR(20) COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员用户 (密码请在生产环境修改)
INSERT INTO `users` (`username`, `password`, `real_name`, `role`, `phone`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', '13800138000', 1),
('inspector1', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'inspector', '13800138001', 1),
('inspector2', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'inspector', '13800138002', 1),
('public1', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'public', '13800138003', 1);

-- =====================================================
-- 2. 水库基础信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `reservoirs` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '水库ID',
    `reservoir_name` VARCHAR(100) NOT NULL COMMENT '水库名称',
    `location` VARCHAR(200) COMMENT '位置',
    `latitude` DECIMAL(10, 6) COMMENT '纬度',
    `longitude` DECIMAL(10, 6) COMMENT '经度',
    `capacity` VARCHAR(50) COMMENT '库容',
    `status` ENUM('normal', 'maintenance', 'risk') DEFAULT 'normal' COMMENT '状态',
    `construction_date` DATE COMMENT '建设日期',
    `last_maintenance_date` DATE COMMENT '最后维护日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_reservoir_name (`reservoir_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水库基础信息表';

-- 插入示例水库数据
INSERT INTO `reservoirs` (`reservoir_name`, `location`, `latitude`, `longitude`, `capacity`, `status`) VALUES
('青山水库', '浙江省杭州市西湖区', 30.246815, 120.139558, '9060万立方米', 'normal'),
('秀水湖水库', '江苏省苏州市吴中区', 31.298441, 120.627423, '4200万立方米', 'normal'),
('碧水源水库', '上海市青浦区', 31.128547, 121.092345, '2800万立方米', 'normal');

-- =====================================================
-- 3. 巡查记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `patrol_records` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `date` DATE COMMENT '巡查日期',
    `time` TIME COMMENT '巡查时间',
    `reservoir_name` VARCHAR(100) COMMENT '水库名称',
    `latitude` DECIMAL(10, 6) COMMENT '纬度',
    `longitude` DECIMAL(10, 6) COMMENT '经度',
    `address` VARCHAR(255) COMMENT '详细地址',
    `inspector` VARCHAR(50) COMMENT '巡查员',
    `inspector_username` VARCHAR(50) COMMENT '巡查员工号',
    `status` ENUM('pending', 'processing', 'completed') DEFAULT 'pending' COMMENT '状态',
    `has_issue` BOOLEAN DEFAULT FALSE COMMENT '是否有问题',
    `issue_type` VARCHAR(50) COMMENT '问题类型',
    `issue_severity` ENUM('low', 'medium', 'high', 'critical') COMMENT '问题严重性',
    `description` TEXT COMMENT '问题描述',
    `has_photo` BOOLEAN DEFAULT FALSE COMMENT '是否有照片',
    `photo_urls` TEXT COMMENT '照片URL数组(JSON)',
    `reporter_name` VARCHAR(50) COMMENT '上报人',
    `reporter_role` ENUM('admin', 'inspector', 'public') COMMENT '上报人角色',
    `assigned_inspector` VARCHAR(50) COMMENT '指派巡查员',
    `assignment_note` TEXT COMMENT '指派备注',
    `assignment_time` DATETIME COMMENT '指派时间',
    `situation_description` TEXT COMMENT '处理情况',
    `processing_result` ENUM('resolved', 'improved', 'ongoing', 'reported') COMMENT '处理结果',
    `completion_time` DATETIME COMMENT '完成时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_date (`date`),
    INDEX idx_status (`status`),
    INDEX idx_inspector (`inspector`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡查记录表';

-- =====================================================
-- 4. 预警记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `warning_records` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    `warning_type` VARCHAR(50) COMMENT '预警类型',
    `warning_level` ENUM('low', 'medium', 'high', 'critical') COMMENT '预警级别',
    `reservoir_id` INT COMMENT '水库ID',
    `reservoir_name` VARCHAR(100) COMMENT '水库名称',
    `latitude` DECIMAL(10, 6) COMMENT '纬度',
    `longitude` DECIMAL(10, 6) COMMENT '经度',
    `description` TEXT COMMENT '预警描述',
    `indicator_value` DECIMAL(10, 4) COMMENT '指标值',
    `threshold_value` DECIMAL(10, 4) COMMENT '阈值',
    `status` ENUM('pending', 'processed', 'dismissed') DEFAULT 'pending' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_level (`warning_level`),
    INDEX idx_status (`status`),
    INDEX idx_created_at (`created_at`),
    FOREIGN KEY (`reservoir_id`) REFERENCES `reservoirs`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- =====================================================
-- 5. 预警阈值配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `warning_thresholds` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `cod_threshold` DECIMAL(10, 4) DEFAULT 40.0000 COMMENT 'COD阈值(mg/L)',
    `ammonia_nitrogen_threshold` DECIMAL(10, 4) DEFAULT 1.5000 COMMENT '氨氮阈值(mg/L)',
    `total_phosphorus_threshold` DECIMAL(10, 4) DEFAULT 0.2000 COMMENT '总磷阈值(mg/L)',
    `total_nitrogen_threshold` DECIMAL(10, 4) DEFAULT 2.0000 COMMENT '总氮阈值(mg/L)',
    `permanganate_threshold` DECIMAL(10, 4) DEFAULT 10.0000 COMMENT '高锰酸盐指数阈值(mg/L)',
    `flood_limit_water_level` DECIMAL(10, 2) DEFAULT 592.00 COMMENT '汛限水位(米)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警阈值配置表';

INSERT INTO `warning_thresholds` (`cod_threshold`, `ammonia_nitrogen_threshold`, `total_phosphorus_threshold`, 
    `total_nitrogen_threshold`, `permanganate_threshold`, `flood_limit_water_level`) VALUES
(40.0000, 1.5000, 0.2000, 2.0000, 10.0000, 592.00);

-- =====================================================
-- 6. 图层配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `layer_configs` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `layer_type` VARCHAR(50) NOT NULL UNIQUE COMMENT '图层类型标识',
    `layer_name` VARCHAR(100) NOT NULL COMMENT '显示名称',
    `color` VARCHAR(20) DEFAULT '#3388FF' COMMENT '颜色',
    `opacity` DECIMAL(3, 2) DEFAULT 0.80 COMMENT '透明度(0-1)',
    `visible` BOOLEAN DEFAULT TRUE COMMENT '是否显示',
    `icon` VARCHAR(50) COMMENT '图标样式',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图层配置表';

-- 插入默认图层配置
INSERT INTO `layer_configs` (`layer_type`, `layer_name`, `color`, `opacity`, `visible`, `icon`) VALUES
('admin_boundaries', '行政区划', '#FF6B6B', 0.80, TRUE, 'polygon'),
('rivers', '河流', '#4ECDC4', 0.80, TRUE, 'line'),
('reservoirs', '水库', '#45B7D1', 0.90, TRUE, 'point'),
('monitor_points', '监测点', '#96CEB4', 1.00, TRUE, 'point'),
('residents', '居民点', '#FFEAA7', 0.80, TRUE, 'point');

-- =====================================================
-- 7. 角色表
-- =====================================================
CREATE TABLE IF NOT EXISTS `roles` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    `description` VARCHAR(255) COMMENT '角色说明',
    `permissions` TEXT COMMENT '权限列表(JSON)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 插入默认角色
INSERT INTO `roles` (`role_name`, `role_key`, `description`, `permissions`) VALUES
('管理员', 'admin', '系统管理员，拥有所有权限', '["dashboard", "data_explorer", "data_management", "field_operations", "system_config", "profile"]'),
('巡查员', 'inspector', '水库巡查人员', '["dashboard", "data_explorer", "field_operations", "profile"]'),
('公众', 'public', '普通公众用户', '["dashboard", "field_operations", "profile"]');

-- =====================================================
-- 8. 模拟参数预设表
-- =====================================================
CREATE TABLE IF NOT EXISTS `simulation_configs` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `diffusion_radius` DECIMAL(10, 2) DEFAULT 1000 COMMENT '扩散半径(米)',
    `decay_coefficient` DECIMAL(5, 4) DEFAULT 0.01 COMMENT '衰减系数',
    `wind_speed` DECIMAL(5, 2) COMMENT '风速(m/s)',
    `wind_direction` INT COMMENT '风向(度)',
    `water_flow_rate` DECIMAL(10, 4) COMMENT '水流流速(m/s)',
    `simulation_duration` INT DEFAULT 3600 COMMENT '模拟时长(秒)',
    `is_default` BOOLEAN DEFAULT FALSE COMMENT '是否为默认配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟参数预设表';

INSERT INTO `simulation_configs` (`config_name`, `diffusion_radius`, `decay_coefficient`, `is_default`) VALUES
('默认配置', 1000, 0.01, TRUE),
('快速扩散', 2000, 0.005, FALSE),
('缓慢扩散', 500, 0.02, FALSE);

-- =====================================================
-- 9. 标注管理表
-- =====================================================
CREATE TABLE IF NOT EXISTS `annotations` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标注ID',
    `user_id` INT COMMENT '用户ID',
    `title` VARCHAR(100) COMMENT '标注标题',
    `content` TEXT COMMENT '标注内容',
    `latitude` DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 6) NOT NULL COMMENT '经度',
    `color` VARCHAR(20) DEFAULT '#FF5722' COMMENT '颜色',
    `icon` VARCHAR(50) DEFAULT 'marker' COMMENT '图标',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标注管理表';

-- =====================================================
-- 10. 水情管理表 (已存在)
-- =====================================================
CREATE TABLE IF NOT EXISTS `water_situation` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `reservoir_name` VARCHAR(100) COMMENT '库名',
    `date` DATETIME COMMENT '日期',
    `water_level` DECIMAL(10, 2) COMMENT '库水位(米)',
    `storage` DECIMAL(12, 2) COMMENT '蓄水量(万立方米)',
    `avg_inflow` DECIMAL(10, 3) COMMENT '日平均入库流量(立方米/秒)',
    `avg_outflow` DECIMAL(10, 3) COMMENT '日平均出库流量(立方米/秒)',
    `yoy_increase` DECIMAL(12, 2) COMMENT '比去年同期增减(万立方米)',
    `total_capacity` DECIMAL(12, 2) COMMENT '总库容(万立方米)',
    `flood_level` DECIMAL(10, 2) COMMENT '汛限水位(米)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_reservoir_date (`reservoir_name`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水情管理表';

-- =====================================================
-- 11. 监测断面管理表 (已存在)
-- =====================================================
CREATE TABLE IF NOT EXISTS `section_monitor` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `monitor_point_name` VARCHAR(100) COMMENT '监测点名称',
    `reservoir_name` VARCHAR(100) COMMENT '水库名称',
    `year` INT COMMENT '年份',
    `month` INT COMMENT '月份',
    `ammonia_nitrogen` DECIMAL(10, 4) COMMENT '氨氮(mg/L)',
    `permanganate_index` DECIMAL(10, 4) COMMENT '高锰酸盐指数(mg/L)',
    `cod` DECIMAL(10, 4) COMMENT '化学需氧量(mg/L)',
    `flow_rate` DECIMAL(10, 4) COMMENT '流量(立方米/秒)',
    `water_depth` DECIMAL(10, 2) COMMENT '水深(米)',
    `total_nitrogen` DECIMAL(10, 4) COMMENT '总氮(mg/L)',
    `total_phosphorus` DECIMAL(10, 4) COMMENT '总磷(mg/L)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_monitor_point (`monitor_point_name`),
    INDEX idx_reservoir_year_month (`reservoir_name`, `year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测断面管理表';

-- =====================================================
-- 10. 巡查任务表
-- =====================================================
CREATE TABLE IF NOT EXISTS `inspection_tasks` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `reservoir_name` VARCHAR(100) COMMENT '水库名称',
    `latitude` DECIMAL(10, 6) COMMENT '纬度',
    `longitude` DECIMAL(10, 6) COMMENT '经度',
    `status` ENUM('pending', 'processing', 'completed') DEFAULT 'pending' COMMENT '状态',
    `creator_id` INT COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名',
    `assignee_id` INT COMMENT '指派人ID',
    `assignee_name` VARCHAR(50) COMMENT '任务执行人',
    `deadline` DATETIME COMMENT '任务截止时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (`status`),
    INDEX idx_assignee (`assignee_id`),
    INDEX idx_deadline (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡查任务表';

-- =====================================================
-- 11. 任务反馈表
-- =====================================================
CREATE TABLE IF NOT EXISTS `task_feedbacks` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    `task_id` INT NOT NULL COMMENT '任务ID',
    `content` TEXT COMMENT '反馈内容',
    `photos` TEXT COMMENT '照片URL数组(JSON)',
    `inspector` VARCHAR(50) COMMENT '反馈人',
    `inspector_username` VARCHAR(50) COMMENT '反馈人账号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`task_id`) REFERENCES `inspection_tasks`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务反馈表';

-- =====================================================
-- 12. 问题上报表
-- =====================================================
CREATE TABLE IF NOT EXISTS `issue_reports` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '上报ID',
    `reservoir_name` VARCHAR(100) COMMENT '水库名称',
    `description` TEXT NOT NULL COMMENT '问题描述',
    `severity` ENUM('low', 'medium', 'high', 'critical') DEFAULT 'medium' COMMENT '严重性',
    `notes` TEXT COMMENT '备注',
    `photos` TEXT COMMENT '照片URL数组(JSON)',
    `latitude` DECIMAL(10, 6) COMMENT '纬度',
    `longitude` DECIMAL(10, 6) COMMENT '经度',
    `address` VARCHAR(255) COMMENT '详细地址',
    `reporter_name` VARCHAR(50) NOT NULL COMMENT '上报人',
    `reporter_role` ENUM('admin', 'inspector', 'public') DEFAULT 'public' COMMENT '上报人角色',
    `reporter_username` VARCHAR(50) COMMENT '上报人账号',
    `status` ENUM('pending', 'processing', 'completed') DEFAULT 'pending' COMMENT '状态',
    `assigned_inspector` VARCHAR(50) COMMENT '指派人',
    `assignment_note` TEXT COMMENT '指派备注',
    `assignment_time` DATETIME COMMENT '指派时间',
    `processing_result` TEXT COMMENT '处理结果',
    `completion_time` DATETIME COMMENT '完成时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (`status`),
    INDEX idx_severity (`severity`),
    INDEX idx_reporter (`reporter_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题上报表';

-- =====================================================
-- 创建完成后显示结果
-- =====================================================
SELECT '数据库初始化完成!' AS message;
SHOW TABLES;
