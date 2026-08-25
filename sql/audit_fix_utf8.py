#!/usr/bin/env python3
"""全面扫描并修复 clarimire 库中文乱码"""
import re
import sys

import pymysql

DB = dict(host='localhost', user='root', password='root', database='clarimire', charset='utf8mb4')

USER_FIXES = {
    'admin': '系统管理员',
    'inspector1': '张巡查',
    'public1': '李群众',
}

WARNING_RULE_FIXES = {
    'water_level': '水位预警',
    'ammonia_nitrogen': '氨氮预警',
    'cod': 'COD预警',
    'total_phosphorus': '总磷预警',
}

# 常见损坏前缀 / 双重编码，统一修正为正确库名
RESERVOIR_REPLACEMENTS = [
    ('??????水库', '密云水库'),
    ('????水库', '密云水库'),
    ('瀵嗕簯姘村簱水库', '密云水库'),
    ('密云水库ˮ库', '密云水库'),
]

TEXT_COLUMNS = [
    ('warning_record', 'reservoir_name'),
    ('warning_record', 'description'),
    ('patrol_record', 'reservoir_name'),
    ('patrol_record', 'remark'),
    ('patrol_task', 'title'),
    ('patrol_task', 'reservoir_name'),
    ('patrol_task', 'assignee_name'),
    ('issue_report', 'title'),
    ('issue_report', 'reporter_name'),
    ('issue_report', 'reservoir_name'),
    ('issue_report', 'description'),
]


def is_suspicious(value):
    if value is None:
        return False
    text = str(value)
    if '?' in text:
        return True
    if '\ufffd' in text or '�' in text:
        return True
    if '瀵' in text or '姘' in text:
        return True
    return False


def audit(cur):
    issues = []
    for table, column in TEXT_COLUMNS:
        cur.execute(
            f"SELECT `{column}` FROM `{table}` WHERE `{column}` IS NOT NULL LIMIT 5000"
        )
        for (value,) in cur.fetchall():
            if is_suspicious(value):
                issues.append((table, column, value))
                break
    return issues


def replace_in_column(cur, table, column, old, new):
    cur.execute(
        f"UPDATE `{table}` SET `{column}`=%s WHERE `{column}`=%s",
        (new, old),
    )
    if cur.rowcount:
        print(f'  {table}.{column}: {old!r} -> {new!r} ({cur.rowcount} rows)')


def replace_like(cur, table, column, pattern, new):
    cur.execute(
        f"UPDATE `{table}` SET `{column}`=%s WHERE `{column}` LIKE %s",
        (new, pattern),
    )
    if cur.rowcount:
        print(f'  {table}.{column} LIKE {pattern!r} -> {new!r} ({cur.rowcount} rows)')


def fix_all(cur):
    print('[1] 用户姓名')
    for username, real_name in USER_FIXES.items():
        cur.execute('UPDATE `user` SET real_name=%s WHERE username=%s', (real_name, username))

    print('[2] 预警规则名称')
    for indicator, rule_name in WARNING_RULE_FIXES.items():
        cur.execute(
            'UPDATE warning_rule SET rule_name=%s WHERE indicator=%s',
            (rule_name, indicator),
        )

    print('[3] 库名乱码 / 双重编码')
    for table, column in TEXT_COLUMNS:
        if 'reservoir' not in column and column != 'reservoir_name':
            continue
        for old, new in RESERVOIR_REPLACEMENTS:
            replace_in_column(cur, table, column, old, new)
        replace_like(cur, table, column, '%?%水库', '密云水库')

    print('[4] 预警记录描述')
    cur.execute(
        "UPDATE warning_record SET description=%s WHERE description LIKE %s",
        ('氨氮超过橙色阈值，需启动预警响应。', '%?%超过橙色阈值%'),
    )
    if cur.rowcount:
        print(f'  warning_record.description fixed ({cur.rowcount} rows)')

    print('[5] 巡查记录姓名（从 user 表同步）')
    cur.execute(
        """
        UPDATE patrol_record pr
        INNER JOIN `user` u ON pr.user_id = u.id
        SET pr.real_name = u.real_name
        WHERE pr.real_name IS NULL OR pr.real_name LIKE '%?%' OR LENGTH(pr.real_name) < 2
        """
    )
    if cur.rowcount:
        print(f'  patrol_record.real_name synced ({cur.rowcount} rows)')

    print('[6] 巡查记录无效 remark')
    cur.execute(
        """
        UPDATE patrol_record
        SET remark = NULL
        WHERE remark LIKE '%?%' AND remark NOT IN ('core', 'buffer', 'remote')
        """
    )
    if cur.rowcount:
        print(f'  patrol_record.remark cleared ({cur.rowcount} rows)')

    print('[7] 巡查任务')
    cur.execute(
        "UPDATE patrol_task SET title=%s, reservoir_name=%s WHERE title LIKE %s OR title=%s",
        ('日常巡查', '密云水库', '%?%', '??'),
    )
    cur.execute(
        """
        UPDATE patrol_task pt
        INNER JOIN `user` u ON pt.assignee_id = u.id
        SET pt.assignee_name = u.real_name
        WHERE pt.assignee_name IS NULL OR pt.assignee_name = 'inspector'
        """
    )
    cur.execute(
        "UPDATE patrol_task SET title=%s WHERE title='Patrol Miyun'",
        ('密云水库巡查',),
    )
    for old, new in RESERVOIR_REPLACEMENTS:
        replace_in_column(cur, 'patrol_task', 'reservoir_name', old, new)

    print('[8] 问题上报示例')
    cur.execute("DELETE FROM issue_report WHERE title LIKE %s OR reporter_name LIKE %s", ('%?%', '%?%'))
    cur.execute(
        """
        INSERT INTO issue_report (title, issue_type, status, reporter_id, reporter_name, reservoir_name, description)
        SELECT %s,'public','pending',3,%s,%s,%s FROM DUAL
        WHERE NOT EXISTS (SELECT 1 FROM issue_report WHERE title=%s)
        """,
        ('密云水库异味', '李群众', '密云水库', '水库东侧有轻微异味', '密云水库异味'),
    )


def verify(cur):
    report = []
    checks = [
        ('user', 'SELECT username, real_name FROM user ORDER BY id'),
        ('warning_rule', 'SELECT rule_name, indicator FROM warning_rule ORDER BY id'),
        ('warning_record', 'SELECT id, reservoir_name, warning_level, description FROM warning_record ORDER BY id'),
        ('patrol_task', 'SELECT id, title, reservoir_name, assignee_name FROM patrol_task ORDER BY id'),
        ('patrol_record', 'SELECT id, real_name, reservoir_name, remark FROM patrol_record ORDER BY id LIMIT 15'),
        ('water_reservoir', 'SELECT reservoir_name FROM water_reservoir ORDER BY id LIMIT 5'),
        ('water_situation', 'SELECT DISTINCT reservoir_name FROM water_situation LIMIT 5'),
        ('section_monitor', 'SELECT monitor_point_name, reservoir_name FROM section_monitor LIMIT 5'),
        ('issue_report', 'SELECT title, reporter_name, reservoir_name FROM issue_report LIMIT 5'),
    ]
    for name, sql in checks:
        cur.execute(sql)
        report.append(f'--- {name} ---')
        for row in cur.fetchall():
            report.append(str(row))
    return report


def main():
    dry_run = '--dry-run' in sys.argv
    conn = pymysql.connect(**DB)
    cur = conn.cursor()

    before = audit(cur)
    print(f'修复前可疑字段: {len(before)}')
    for item in before:
        print(' ', item)

    if dry_run:
        conn.close()
        return

    fix_all(cur)
    conn.commit()

    after = audit(cur)
    print(f'\n修复后可疑字段: {len(after)}')
    for item in after:
        print(' ', item)

    print('\n=== 抽样验证 ===')
    for line in verify(cur):
        print(line)

    conn.close()
    print('\n完成。若 Web 仍显示旧数据，请重启 8080 后端并 Ctrl+F5 强刷。')


if __name__ == '__main__':
    main()
