package com.clarimire.controller;

import com.clarimire.entity.PatrolRecord;
import com.clarimire.entity.AssignRequest;
import com.clarimire.service.PatrolService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/web/patrol")
@CrossOrigin(origins = "*")
public class WebPatrolController {

    @Autowired
    private PatrolService patrolService;

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
