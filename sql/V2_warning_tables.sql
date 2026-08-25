-- =========================================
-- Clarimire V2 - 预警与水库元数据
-- =========================================
USE clarimire;

-- 预警规则表
CREATE TABLE IF NOT EXISTS `warning_rule` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `indicator` VARCHAR(50) NOT NULL COMMENT '指标: water_level/ammonia_nitrogen/cod/total_phosphorus',
    `yellow_threshold` DECIMAL(10,3) DEFAULT NULL COMMENT '黄色阈值',
    `orange_threshold` DECIMAL(10,3) DEFAULT NULL COMMENT '橙色阈值',
    `red_threshold` DECIMAL(10,3) DEFAULT NULL COMMENT '红色阈值',
    `enabled` TINYINT(1) DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则';

-- 预警记录表
CREATE TABLE IF NOT EXISTS `warning_record` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `reservoir_name` VARCHAR(100) NOT NULL,
    `warning_type` VARCHAR(50) NOT NULL COMMENT '水位预警/水质异常',
    `warning_level` VARCHAR(20) NOT NULL COMMENT '蓝色/黄色/橙色/红色',
    `indicator` VARCHAR(50) DEFAULT NULL,
    `current_value` DECIMAL(10,3) DEFAULT NULL,
    `threshold_value` DECIMAL(10,3) DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `suggestion` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_reservoir` (`reservoir_name`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录';

-- 默认预警规则
INSERT INTO `warning_rule` (`rule_name`, `indicator`, `yellow_threshold`, `orange_threshold`, `red_threshold`) VALUES
('水位预警', 'water_level', NULL, NULL, NULL),
('氨氮预警', 'ammonia_nitrogen', 0.5, 1.0, 2.0),
('COD预警', 'cod', 15.0, 20.0, 30.0),
('总磷预警', 'total_phosphorus', 0.05, 0.1, 0.2);

-- 18座水库元数据（来自预警分析模块）
INSERT INTO `water_reservoir` (`reservoir_name`, `short_name`, `flood_level`, `max_level`, `avg_level`, `capacity`, `status`) VALUES
('白河堡水库', '白河堡', 592.6, 599.13, 591.6, 9060, 1),
('半城子水库', '半城子', 255.0, 255.0, NULL, NULL, 1),
('北台上水库', '北台上', 85.0, 84.8, NULL, NULL, 1),
('崇青水库', '崇青', 71.5, 71.5, NULL, NULL, 1),
('大宁水库', '大宁', 48.0, 59.29, NULL, NULL, 1),
('大水峪水库', '大水峪', 166.4, 168.9, NULL, NULL, 1),
('官厅水库', '官厅', 476.0, 497.0, NULL, 12000, 1),
('海子水库', '海子', 106.5, 108.5, NULL, NULL, 1),
('怀柔水库', '怀柔', 58.0, 62.13, NULL, 1200, 1),
('黄松峪水库', '黄松峪', 203.0, 203.0, NULL, NULL, 1),
('密云水库', '密云', 152.0, 155.59, 151.8, 43100, 1),
('沙厂水库', '沙厂', 165.5, 165.5, NULL, NULL, 1),
('十三陵水库', '十三陵', 93.0, 91.81, NULL, 7450, 1),
('桃峪口水库', '桃峪口', 67.7, 70.23, NULL, NULL, 1),
('西峪水库', '西峪', 213.5, 213.5, NULL, NULL, 1),
('遥桥峪水库', '遥桥峪', 464.0, 464.0, NULL, NULL, 1),
('斋堂水库', '斋堂', 453.0, 461.56, NULL, NULL, 1),
('珠窝水库', '珠窝', 348.4, 352.5, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE
    short_name = VALUES(short_name),
    flood_level = VALUES(flood_level),
    max_level = VALUES(max_level),
    avg_level = VALUES(avg_level);
