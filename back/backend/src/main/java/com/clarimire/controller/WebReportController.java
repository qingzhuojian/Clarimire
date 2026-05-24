package com.clarimire.controller;

import com.clarimire.entity.IssueReport;
import com.clarimire.entity.ProcessReportRequest;
import com.clarimire.entity.ReportToTaskRequest;
import com.clarimire.service.IssueReportService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/reports")
@CrossOrigin(origins = "*")
public class WebReportController {

    @Autowired
    private IssueReportService reportService;

    @GetMapping
    public PageInfo<IssueReport> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String severity,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate) {
        return reportService.getReports(page, pageSize, status, severity, null, startDate, endDate);
    }

    @GetMapping("/{id}")
    public IssueReport getById(@PathVariable Integer id) {
        return reportService.getReportById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody IssueReport report) {
        reportService.createReport(report);
        return ResponseEntity.ok(Map.of("message", "上报成功", "reportId", report.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody IssueReport report) {
        report.setId(id);
        reportService.updateReport(report);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @PostMapping("/{id}/to-task")
    public ResponseEntity<?> convertToTask(@PathVariable Integer id, @RequestBody ReportToTaskRequest request) {
        boolean success = reportService.convertToTask(id, request.getAssigneeId(), request.getAssigneeName(), request.getNote());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "已转为任务"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "转换失败"));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Integer id, @RequestBody ProcessReportRequest request) {
        boolean success = reportService.processReport(id, request.getProcessingResult());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "已标记解决"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "操作失败"));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable Integer id, @RequestBody ProcessReportRequest request) {
        boolean success = reportService.assignReport(id, request.getAssignedInspector(), request.getAssignmentNote());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "指派成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "指派失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
