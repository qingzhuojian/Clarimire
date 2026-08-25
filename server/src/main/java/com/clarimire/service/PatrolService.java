package com.clarimire.service;

import com.clarimire.entity.PatrolRecord;
import com.clarimire.entity.PatrolTask;

import java.util.List;
import java.util.Map;

public interface PatrolService {
    List<PatrolTask> getTasks(String status, Integer assigneeId, String reservoirName, String taskType);

    PatrolTask getTaskById(Integer id);

    boolean createTask(PatrolTask task);

    boolean updateTask(PatrolTask task);

    boolean deleteTask(Integer id);

    List<PatrolRecord> getRecords(Integer userId, String reservoirName, String locationZone,
                                  String startDate, String endDate);

    Map<String, Object> checkin(PatrolRecord record, String username);

    Map<String, Object> completeTask(Integer taskId, String username);

    Map<String, Object> ensureDailyTasks(Integer assigneeId);

    Map<String, Object> getDashboardStats();
}
