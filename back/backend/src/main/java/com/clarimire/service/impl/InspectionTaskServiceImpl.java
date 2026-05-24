package com.clarimire.service.impl;

import com.clarimire.entity.InspectionTask;
import com.clarimire.entity.TaskFeedback;
import com.clarimire.mapper.InspectionTaskMapper;
import com.clarimire.mapper.TaskFeedbackMapper;
import com.clarimire.service.InspectionTaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InspectionTaskServiceImpl implements InspectionTaskService {

    @Autowired
    private InspectionTaskMapper taskMapper;

    @Autowired
    private TaskFeedbackMapper feedbackMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createTask(InspectionTask task) {
        if (task.getStatus() == null) {
            task.setStatus("pending");
        }
        LocalDateTime now = LocalDateTime.now();
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(now);
        }
        if (task.getUpdatedAt() == null) {
            task.setUpdatedAt(now);
        }
        taskMapper.insert(task);
    }

    @Override
    public InspectionTask getTaskById(Integer id) {
        return taskMapper.findById(id);
    }

    @Override
    public PageInfo<InspectionTask> getTasks(int page, int pageSize, String status, Integer assigneeId, String assigneeName) {
        PageHelper.startPage(page, pageSize);
        List<InspectionTask> tasks = taskMapper.findAll(status, assigneeId, assigneeName, null);
        return new PageInfo<>(tasks);
    }

    @Override
    public List<InspectionTask> getPendingTasksByAssignee(Integer assigneeId) {
        return taskMapper.findPendingByAssignee(assigneeId);
    }

    @Override
    public void updateTask(InspectionTask task) {
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);
    }

    @Override
    public void deleteTask(Integer id) {
        feedbackMapper.deleteByTaskId(id);
        taskMapper.deleteById(id);
    }

    @Override
    @Transactional
    public boolean acceptTask(Integer id, Integer assigneeId, String assigneeName) {
        InspectionTask task = taskMapper.findById(id);
        if (task == null) {
            return false;
        }
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assigneeName);
        task.setStatus("processing");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);
        return true;
    }

    @Override
    @Transactional
    public boolean submitFeedback(Integer id, TaskFeedback feedback) {
        InspectionTask task = taskMapper.findById(id);
        if (task == null) {
            return false;
        }
        feedback.setTaskId(id);
        feedback.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        feedbackMapper.insert(feedback);
        return true;
    }

    @Override
    @Transactional
    public boolean completeTask(Integer id, String processingResult) {
        InspectionTask task = taskMapper.findById(id);
        if (task == null) {
            return false;
        }
        task.setStatus("completed");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        TaskFeedback feedback = new TaskFeedback();
        feedback.setContent(processingResult);
        feedback.setTaskId(id);
        feedback.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        feedbackMapper.insert(feedback);
        return true;
    }

    @Override
    public int countPendingTasks() {
        return taskMapper.countByStatus("pending");
    }
}
