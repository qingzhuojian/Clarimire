package com.clarimire.controller;

import com.clarimire.entity.IssueReport;
import com.clarimire.entity.User;
import com.clarimire.service.IssueService;
import com.clarimire.service.UserService;
import com.clarimire.util.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) Integer reporterId) {
        Map<String, Object> result = new HashMap<>();
        List<IssueReport> list = issueService.getList(status, issueType, reporterId);
        result.put("code", 200);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        IssueReport report = issueService.getById(id);
        if (report != null) {
            result.put("code", 200);
            result.put("data", report);
        } else {
            result.put("code", 404);
            result.put("message", "记录不存在");
        }
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody IssueReport report, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String username = AuthContext.getUsername(request);
        User user = userService.findByUsername(username);
        if (user != null) {
            report.setReporterId(user.getId());
            if (report.getReporterName() == null) {
                report.setReporterName(user.getRealName());
            }
        }
        if (issueService.create(report)) {
            result.put("code", 200);
            result.put("message", "提交成功");
        } else {
            result.put("code", 400);
            result.put("message", "提交失败");
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody IssueReport report) {
        Map<String, Object> result = new HashMap<>();
        if (issueService.update(report)) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 400);
            result.put("message", "更新失败");
        }
        return result;
    }

    @PostMapping("/review")
    public Map<String, Object> review(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer id = (Integer) body.get("id");
        String action = (String) body.get("action");
        String adminRemark = (String) body.get("adminRemark");
        Integer assigneeId = body.get("assigneeId") != null ? ((Number) body.get("assigneeId")).intValue() : null;
        String operator = AuthContext.getUsername(request);

        Map<String, Object> reviewResult = issueService.review(id, action, adminRemark, assigneeId, operator);
        if (Boolean.TRUE.equals(reviewResult.get("success"))) {
            result.put("code", 200);
            result.put("message", reviewResult.get("message"));
            result.put("data", reviewResult.get("data"));
        } else {
            result.put("code", 400);
            result.put("message", reviewResult.get("message"));
        }
        return result;
    }
}
