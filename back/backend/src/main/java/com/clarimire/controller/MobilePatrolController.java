package com.clarimire.controller;

import com.clarimire.entity.CheckinRequest;
import com.clarimire.entity.PatrolRecord;
import com.clarimire.entity.ReportRequest;
import com.clarimire.service.PatrolService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/patrol")
@CrossOrigin(origins = "*")
public class MobilePatrolController {

    @Autowired
    private PatrolService patrolService;

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@RequestBody CheckinRequest request) {
        PatrolRecord record = new PatrolRecord();
        record.setLatitude(request.getLat() != null ? java.math.BigDecimal.valueOf(request.getLat()) : null);
        record.setLongitude(request.getLng() != null ? java.math.BigDecimal.valueOf(request.getLng()) : null);
        record.setAddress(request.getAddress());
        record.setInspector(request.getInspector());
        record.setDate(LocalDate.now().atStartOfDay());
        record.setTime(LocalTime.now().atDate(java.time.LocalDate.now()));
        record.setStatus("pending");
        record.setHasIssue(false);
        record.setCreatedAt(LocalDateTime.now());

        patrolService.createRecord(record);
        return ResponseEntity.ok(Map.of("message", "签到成功", "recordId", record.getId()));
    }

    @GetMapping("/records")
    public PageInfo<PatrolRecord> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String inspector) {
        return patrolService.getRecords(page, pageSize, status, inspector, null, null);
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
        record.setLatitude(request.getLatitude() != null ? java.math.BigDecimal.valueOf(request.getLatitude()) : null);
        record.setLongitude(request.getLongitude() != null ? java.math.BigDecimal.valueOf(request.getLongitude()) : null);
        record.setAddress(request.getAddress());
        record.setStatus("pending");
        record.setDate(LocalDate.now().atStartOfDay());
        record.setTime(LocalTime.now().atDate(java.time.LocalDate.now()));
        record.setCreatedAt(LocalDateTime.now());

        patrolService.createRecord(record);
        return ResponseEntity.ok(Map.of("message", "上报成功", "recordId", record.getId()));
    }
}
