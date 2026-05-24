package com.clarimire.controller;

import com.clarimire.entity.InspectionTask;
import com.clarimire.entity.CreateTaskRequest;
import com.clarimire.service.InspectionTaskService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/tasks")
@CrossOrigin(origins = "*")
public class WebTaskController {

    @Autowired
    private InspectionTaskService taskService;

    @GetMapping
    public PageInfo<InspectionTask> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) Integer assigneeId,
                                       @RequestParam(required = false) String assigneeName) {
        return taskService.getTasks(page, pageSize, status, assigneeId, assigneeName);
    }

    @GetMapping("/{id}")
    public InspectionTask getById(@PathVariable Integer id) {
        return taskService.getTaskById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateTaskRequest request) {
        InspectionTask task = new InspectionTask();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setReservoirName(request.getReservoirName());
        if (request.getLatitude() != null) {
            task.setLatitude(java.math.BigDecimal.valueOf(request.getLatitude()));
        }
        if (request.getLongitude() != null) {
            task.setLongitude(java.math.BigDecimal.valueOf(request.getLongitude()));
        }
        task.setAssigneeId(request.getAssigneeId());
        task.setAssigneeName(request.getAssigneeName());
        if (request.getDeadline() != null) {
            task.setDeadline(java.time.LocalDateTime.parse(request.getDeadline().replace(" ", "T")));
        }
        task.setStatus("pending");

        taskService.createTask(task);
        return ResponseEntity.ok(Map.of("message", "任务创建成功", "taskId", task.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody InspectionTask task) {
        task.setId(id);
        taskService.updateTask(task);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
