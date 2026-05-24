package com.clarimire.controller;

import com.clarimire.entity.*;
import com.clarimire.service.PatrolService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/patrol")
@CrossOrigin(origins = "*")
public class PatrolController {

    @Autowired
    private PatrolService patrolService;

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@RequestBody CheckinRequest request) {
        PatrolRecord record = new PatrolRecord();
        record.setLatitude(BigDecimal.valueOf(request.getLat()));
        record.setLongitude(BigDecimal.valueOf(request.getLng()));
        record.setAddress(request.getAddress());
        record.setInspector(request.getInspector());
        record.setDate(LocalDateTime.now());
        record.setTime(LocalDateTime.now());
        record.setStatus("pending");
        record.setHasIssue(false);

        patrolService.createRecord(record);
        return ResponseEntity.ok(Map.of("message", "签到成功", "recordId", record.getId()));
    }

    @GetMapping("/records")
    public PageInfo<PatrolRecord> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String inspector,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return patrolService.getRecords(page, pageSize, status, inspector, startDate, endDate);
    }

    @GetMapping("/records/{id}")
    public PatrolRecord getById(@PathVariable Integer id) {
        return patrolService.getRecordById(id);
    }

    @PostMapping("/report")
    public ResponseEntity<?> report(@RequestBody ReportRequest request) {
        PatrolRecord record = new PatrolRecord();
        record.setReservoirName(request.getReservoirName());
        record.setDescription(request.getDescription());
        record.setPhotoUrls(request.getPhotos());
        record.setHasPhoto(request.getPhotos() != null && !request.getPhotos().isEmpty());
        record.setIssueSeverity(request.getSeverity());
        record.setHasIssue(true);
        record.setReporterName(request.getReporterName());
        record.setReporterRole(request.getReporterRole());
        record.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        record.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        record.setStatus("pending");
        record.setDate(LocalDateTime.now());
        record.setTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        patrolService.createRecord(record);
        return ResponseEntity.ok(Map.of("message", "上报成功", "recordId", record.getId()));
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assign(@RequestBody AssignRequest request) {
        boolean success = patrolService.assignTask(request.getRecordId(), request.getInspector(), request.getNote());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "任务派发成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "派发失败"));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody PatrolRecord record) {
        record.setId(id);
        patrolService.updateRecord(record);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @PutMapping("/records/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String situationDescription) {
        boolean success = patrolService.updateStatus(id, status, situationDescription);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        patrolService.deleteRecord(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
