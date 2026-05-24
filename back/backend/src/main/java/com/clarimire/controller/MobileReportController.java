package com.clarimire.controller;

import com.clarimire.entity.IssueReport;
import com.clarimire.service.IssueReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/reports")
@CrossOrigin(origins = "*")
public class MobileReportController {

    @Autowired
    private IssueReportService reportService;

    @GetMapping("/my")
    public List<IssueReport> getMyReports(@RequestParam String reporter) {
        return reportService.getMyReports(reporter);
    }

    @GetMapping("/pending")
    public List<IssueReport> getPendingReports() {
        return reportService.getPendingReports();
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

    @PostMapping("/{id}/process")
    public ResponseEntity<?> processReport(@PathVariable Integer id,
                                          @RequestBody IssueReport request) {
        boolean success = reportService.processReport(id, request.getProcessingResult());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "处理成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "处理失败"));
    }
}
