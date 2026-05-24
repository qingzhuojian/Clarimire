package com.clarimire.controller;

import com.clarimire.entity.InspectionTask;
import com.clarimire.entity.TaskFeedback;
import com.clarimire.entity.TaskFeedbackRequest;
import com.clarimire.entity.TaskCompleteRequest;
import com.clarimire.service.InspectionTaskService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/tasks")
@CrossOrigin(origins = "*")
public class MobileTaskController {

    @Autowired
    private InspectionTaskService taskService;

    @GetMapping("/pending")
    public List<InspectionTask> getPendingTasks(@RequestParam(required = false) Integer inspectorId,
                                                @RequestParam(required = false) String inspector) {
        if (inspectorId != null) {
            return taskService.getPendingTasksByAssignee(inspectorId);
        }
        return taskService.getPendingTasksByAssignee(null);
    }

    @GetMapping("/list")
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

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptTask(@PathVariable Integer id,
                                       @RequestParam Integer assigneeId,
                                       @RequestParam String assigneeName) {
        boolean success = taskService.acceptTask(id, assigneeId, assigneeName);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "任务接受成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "任务接受失败"));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Integer id,
                                          @RequestBody TaskFeedbackRequest request) {
        TaskFeedback feedback = new TaskFeedback();
        feedback.setContent(request.getContent());
        feedback.setPhotos(request.getPhotos());
        feedback.setInspector(request.getInspector());
        feedback.setInspectorUsername(request.getInspectorUsername());

        boolean success = taskService.submitFeedback(id, feedback);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "反馈提交成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "反馈提交失败"));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Integer id,
                                        @RequestBody TaskCompleteRequest request) {
        boolean success = taskService.completeTask(id, request.getProcessingResult());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "任务完成成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "任务完成失败"));
    }
}
