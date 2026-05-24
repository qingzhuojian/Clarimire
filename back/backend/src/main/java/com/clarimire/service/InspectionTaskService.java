package com.clarimire.service;

import com.clarimire.entity.InspectionTask;
import com.clarimire.entity.TaskFeedback;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface InspectionTaskService {
    void createTask(InspectionTask task);
    InspectionTask getTaskById(Integer id);
    PageInfo<InspectionTask> getTasks(int page, int pageSize, String status, Integer assigneeId, String assigneeName);
    List<InspectionTask> getPendingTasksByAssignee(Integer assigneeId);
    void updateTask(InspectionTask task);
    void deleteTask(Integer id);
    boolean acceptTask(Integer id, Integer assigneeId, String assigneeName);
    boolean submitFeedback(Integer id, TaskFeedback feedback);
    boolean completeTask(Integer id, String processingResult);
    int countPendingTasks();
}
