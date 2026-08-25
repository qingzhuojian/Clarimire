-- =========================================
-- Clarimire V3 运维调度 / 移动端打卡
-- 执行（务必 UTF-8）:
-- Get-Content -Raw -Encoding UTF8 V3_patrol_tables.sql | mysql --default-character-set=utf8mb4 -u root -proot clarimire
-- =========================================

USE clarimire;

-- 用户表扩展（重复执行若列已存在会报错，可忽略）
ALTER TABLE `user`
    ADD COLUMN `allow_remote_checkin` TINYINT(1) DEFAULT 0 COMMENT '允许异地打卡' AFTER `status`,
    ADD COLUMN `mobile_enabled` TINYINT(1) DEFAULT 1 COMMENT '允许移动端登录' AFTER `allow_remote_checkin`;

-- 巡查任务
CREATE TABLE IF NOT EXISTS `patrol_task` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `task_type` VARCHAR(32) NOT NULL DEFAULT 'assigned' COMMENT 'daily/assigned/emergency',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/assigned/in_progress/completed/cancelled',
    `reservoir_name` VARCHAR(100) DEFAULT NULL COMMENT '目标水库',
    `assignee_id` INT(11) DEFAULT NULL COMMENT '巡查员用户ID',
    `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '巡查员姓名',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
    `due_time` DATETIME DEFAULT NULL COMMENT '截止时间',
    `description` TEXT DEFAULT NULL COMMENT '任务说明',
    `warning_record_id` INT(11) DEFAULT NULL COMMENT '关联预警记录',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_assignee` (`assignee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_reservoir` (`reservoir_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡查任务';

-- 巡查打卡记录
CREATE TABLE IF NOT EXISTS `patrol_record` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `task_id` INT(11) DEFAULT NULL COMMENT '关联任务',
    `user_id` INT(11) NOT NULL,
    `username` VARCHAR(50) NOT NULL,
    `real_name` VARCHAR(50) DEFAULT NULL,
    `reservoir_name` VARCHAR(100) DEFAULT NULL,
    `lat` DECIMAL(10,6) DEFAULT NULL,
    `lng` DECIMAL(10,6) DEFAULT NULL,
    `location_zone` VARCHAR(16) DEFAULT NULL COMMENT 'core/buffer/remote',
    `checkin_mode` VARCHAR(16) DEFAULT 'gps' COMMENT 'gps/manual/remote',
    `distance_m` INT(11) DEFAULT NULL COMMENT '距参考点距离(米)',
    `photos` TEXT DEFAULT NULL COMMENT 'JSON数组',
    `remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_zone` (`location_zone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡查打卡记录';

-- 问题/群众上报
CREATE TABLE IF NOT EXISTS `issue_report` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `issue_type` VARCHAR(32) DEFAULT 'public' COMMENT 'public/emergency/patrol',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/reviewing/assigned/processing/resolved/closed',
    `reporter_id` INT(11) DEFAULT NULL,
    `reporter_name` VARCHAR(50) DEFAULT NULL,
    `reservoir_name` VARCHAR(100) DEFAULT NULL,
    `lat` DECIMAL(10,6) DEFAULT NULL,
    `lng` DECIMAL(10,6) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `photos` TEXT DEFAULT NULL COMMENT 'JSON数组',
    `assigned_to` INT(11) DEFAULT NULL COMMENT '指派巡查员',
    `assigned_name` VARCHAR(50) DEFAULT NULL,
    `patrol_task_id` INT(11) DEFAULT NULL COMMENT '转任务后ID',
    `admin_remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_reporter` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题上报';

-- 打卡策略
CREATE TABLE IF NOT EXISTS `checkin_policy` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `core_radius_m` INT(11) NOT NULL DEFAULT 200 COMMENT '核心区半径(米)',
    `buffer_radius_m` INT(11) NOT NULL DEFAULT 500 COMMENT '缓冲圈半径(米)',
    `remote_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否允许异地补卡',
    `demo_mode` TINYINT(1) DEFAULT 0 COMMENT '演示模式(放宽校验)',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡策略';

-- 水库参考坐标(打卡距离判定)
CREATE TABLE IF NOT EXISTS `reservoir_location` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `reservoir_name` VARCHAR(100) NOT NULL,
    `lat` DECIMAL(10,6) NOT NULL,
    `lng` DECIMAL(10,6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`reservoir_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水库参考坐标';

-- 异地打卡审计
CREATE TABLE IF NOT EXISTS `patrol_audit` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `record_id` INT(11) NOT NULL,
    `action` VARCHAR(32) NOT NULL COMMENT 'approve/reject/revoke_permission',
    `operator` VARCHAR(50) DEFAULT NULL,
    `remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡审计';

-- 默认打卡策略
INSERT INTO `checkin_policy` (`core_radius_m`, `buffer_radius_m`, `remote_enabled`, `demo_mode`)
SELECT 200, 500, 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `checkin_policy` LIMIT 1);

-- 测试账号 (密码见 C239/账号密码.txt)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `allow_remote_checkin`, `mobile_enabled`)
SELECT 'inspector1', '1cbee3b9dede593141be4b2b8e63444d', '张巡查', 'inspector', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'inspector1');

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `allow_remote_checkin`, `mobile_enabled`)
SELECT 'public1', 'b43e2d8f12118112930576d814399841', '李群众', 'public', 0, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'public1');

-- 水库参考坐标 (近似中心点)
INSERT INTO `reservoir_location` (`reservoir_name`, `lat`, `lng`) VALUES
('密云水库', 40.485000, 116.845000),
('怀柔水库', 40.328000, 116.635000),
('官厅水库', 40.258000, 115.608000),
('十三陵水库', 40.296000, 116.248000),
('半城子水库', 40.655000, 117.055000),
('斋堂水库', 39.972000, 115.688000),
('白河堡水库', 40.635000, 116.162000),
('北台上水库', 40.382000, 116.658000),
('崇青水库', 39.802000, 116.102000),
('大宁水库', 39.808000, 116.218000),
('大水峪水库', 40.418000, 116.682000),
('海子水库', 40.178000, 117.118000),
('黄松峪水库', 40.238000, 117.242000),
('沙厂水库', 40.352000, 116.798000),
('桃峪口水库', 40.232000, 116.438000),
('西峪水库', 40.158000, 117.092000),
('遥桥峪水库', 40.662000, 117.395000),
('珠窝水库', 39.968000, 115.818000)
ON DUPLICATE KEY UPDATE lat = VALUES(lat), lng = VALUES(lng);
