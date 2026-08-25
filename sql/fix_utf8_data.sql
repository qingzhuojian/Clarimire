-- 全面修复 clarimire 库中文乱码（UTF-8）
-- 推荐执行: python sql/audit_fix_utf8.py
-- 或 PowerShell（务必 UTF-8）:
-- Get-Content -Raw -Encoding UTF8 sql/fix_utf8_data.sql | mysql --default-character-set=utf8mb4 -u root -proot clarimire

USE clarimire;

-- 用户姓名
UPDATE `user` SET real_name = '系统管理员' WHERE username = 'admin';
UPDATE `user` SET real_name = '张巡查' WHERE username = 'inspector1';
UPDATE `user` SET real_name = '李群众' WHERE username = 'public1';

-- 预警规则
UPDATE warning_rule SET rule_name = '水位预警' WHERE indicator = 'water_level';
UPDATE warning_rule SET rule_name = '氨氮预警' WHERE indicator = 'ammonia_nitrogen';
UPDATE warning_rule SET rule_name = 'COD预警' WHERE indicator = 'cod';
UPDATE warning_rule SET rule_name = '总磷预警' WHERE indicator = 'total_phosphorus';

-- 库名乱码（问号 / 双重编码）
UPDATE warning_record SET reservoir_name = '密云水库' WHERE reservoir_name LIKE '%?%' OR reservoir_name LIKE '%瀵%';
UPDATE patrol_record SET reservoir_name = '密云水库' WHERE reservoir_name LIKE '%?%' OR reservoir_name LIKE '%瀵%';
UPDATE patrol_task SET reservoir_name = '密云水库' WHERE reservoir_name LIKE '%?%' OR reservoir_name LIKE '%瀵%';

UPDATE warning_record SET description = '氨氮超过橙色阈值，需启动预警响应。' WHERE description LIKE '%?%超过橙色阈值%';

UPDATE patrol_record pr
INNER JOIN `user` u ON pr.user_id = u.id
SET pr.real_name = u.real_name
WHERE pr.real_name LIKE '%?%' OR pr.real_name IS NULL;

UPDATE patrol_record SET remark = NULL WHERE remark LIKE '%?%' AND remark NOT IN ('core', 'buffer', 'remote');

UPDATE patrol_task SET title = '日常巡查', reservoir_name = '密云水库' WHERE title LIKE '%?%' OR title = '??';
UPDATE patrol_task SET title = '密云水库巡查' WHERE title = 'Patrol Miyun';
UPDATE patrol_task pt
INNER JOIN `user` u ON pt.assignee_id = u.id
SET pt.assignee_name = u.real_name
WHERE pt.assignee_name IS NULL OR pt.assignee_name = 'inspector';

DELETE FROM issue_report WHERE title LIKE '%?%' OR reporter_name LIKE '%?%';

INSERT INTO issue_report (title, issue_type, status, reporter_id, reporter_name, reservoir_name, description)
SELECT '密云水库异味', 'public', 'pending', 3, '李群众', '密云水库', '水库东侧有轻微异味'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM issue_report WHERE title = '密云水库异味');
