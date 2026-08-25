package com.clarimire.controller;

import com.clarimire.entity.PatrolRecord;
import com.clarimire.entity.PatrolTask;
import com.clarimire.service.PatrolService;
import com.clarimire.util.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patrol")
public class PatrolController {

    @Autowired
    private PatrolService patrolService;

    @GetMapping("/tasks")
    public Map<String, Object> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) String reservoirName,
            @RequestParam(required = false) String taskType,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String role = AuthContext.getRole(request);
        if ("inspector".equals(role) && assigneeId == null) {
            // mobile: only own tasks - need user id from username
            // assigneeId resolved client-side or we filter by username in service - for now client passes assigneeId
        }
        List<PatrolTask> list = patrolService.getTasks(status, assigneeId, reservoirName, taskType);
        result.put("code", 200);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/tasks/{id}")
    public Map<String, Object> getTask(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        PatrolTask task = patrolService.getTaskById(id);
        if (task != null) {
            result.put("code", 200);
            result.put("data", task);
        } else {
            result.put("code", 404);
            result.put("message", "任务不存在");
        }
        return result;
    }

    @PostMapping("/tasks")
    public Map<String, Object> createTask(@RequestBody PatrolTask task, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (task.getCreatedBy() == null) {
            task.setCreatedBy(AuthContext.getUsername(request));
        }
        if (patrolService.createTask(task)) {
            result.put("code", 200);
            result.put("message", "创建成功");
        } else {
            result.put("code", 400);
            result.put("message", "创建失败");
        }
        return result;
    }

    @PutMapping("/tasks")
    public Map<String, Object> updateTask(@RequestBody PatrolTask task) {
        Map<String, Object> result = new HashMap<>();
        if (patrolService.updateTask(task)) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 400);
            result.put("message", "更新失败（完成任务前须先打卡）");
        }
        return result;
    }

    @PostMapping("/tasks/{id}/complete")
    public Map<String, Object> completeTask(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> done = patrolService.completeTask(id, AuthContext.getUsername(request));
        if (Boolean.TRUE.equals(done.get("success"))) {
            result.put("code", 200);
            result.put("message", done.get("message"));
            result.put("data", done.get("data"));
        } else {
            result.put("code", 400);
            result.put("message", done.get("message"));
        }
        return result;
    }

    @PostMapping("/tasks/ensure-daily")
    public Map<String, Object> ensureDaily(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        Integer assigneeId = body.get("assigneeId") != null
                ? ((Number) body.get("assigneeId")).intValue() : null;
        Map<String, Object> ensured = patrolService.ensureDailyTasks(assigneeId);
        if (Boolean.TRUE.equals(ensured.get("success"))) {
            result.put("code", 200);
            result.put("data", ensured.get("data"));
        } else {
            result.put("code", 400);
            result.put("message", ensured.get("message"));
        }
        return result;
    }

    @DeleteMapping("/tasks/{id}")
    public Map<String, Object> deleteTask(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (patrolService.deleteTask(id)) {
            result.put("code", 200);
            result.put("message", "删除成功");
        } else {
            result.put("code", 400);
            result.put("message", "删除失败");
        }
        return result;
    }

    @GetMapping("/records")
    public Map<String, Object> listRecords(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String reservoirName,
            @RequestParam(required = false) String locationZone,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<PatrolRecord> list = patrolService.getRecords(userId, reservoirName, locationZone, startDate, endDate);
        result.put("code", 200);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @PostMapping("/checkin")
    public Map<String, Object> checkin(@RequestBody PatrolRecord record, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String username = AuthContext.getUsername(request);
        Map<String, Object> checkinResult = patrolService.checkin(record, username);
        if (Boolean.TRUE.equals(checkinResult.get("success"))) {
            result.put("code", 200);
            result.put("message", checkinResult.get("message"));
            result.put("data", checkinResult.get("record"));
            result.put("locationZone", checkinResult.get("locationZone"));
            result.put("distanceM", checkinResult.get("distanceM"));
        } else {
            result.put("code", 400);
            result.put("message", checkinResult.get("message"));
            result.put("locationZone", checkinResult.get("locationZone"));
            result.put("distanceM", checkinResult.get("distanceM"));
        }
        return result;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", patrolService.getDashboardStats());
        return result;
    }
}
