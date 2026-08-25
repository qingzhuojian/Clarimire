#!/usr/bin/env python3
"""修复数据库中文乱码（问号）数据"""
import pymysql

conn = pymysql.connect(
    host='localhost', user='root', password='root',
    database='clarimire', charset='utf8mb4'
)
cur = conn.cursor()
cur.execute("UPDATE user SET real_name=%s WHERE username='admin'", ('系统管理员',))
cur.execute("UPDATE user SET real_name=%s WHERE username='inspector1'", ('张巡查',))
cur.execute("UPDATE user SET real_name=%s WHERE username='public1'", ('李群众',))
cur.execute("DELETE FROM issue_report WHERE title LIKE %s OR reporter_name LIKE %s", ('%?%', '%?%'))
cur.execute(
    "INSERT INTO issue_report (title, issue_type, status, reporter_id, reporter_name, reservoir_name, description) "
    "SELECT %s,'public','pending',3,%s,%s,%s FROM DUAL "
    "WHERE NOT EXISTS (SELECT 1 FROM issue_report WHERE title=%s)",
    ('密云水库异味', '李群众', '密云水库', '水库东侧有轻微异味', '密云水库异味')
)
conn.commit()
cur.execute("SELECT username, real_name FROM user")
print('users:', cur.fetchall())
cur.execute("SELECT title, reporter_name, reservoir_name FROM issue_report")
print('issues:', cur.fetchall())
conn.close()
print('done')
