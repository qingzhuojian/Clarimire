package com.clarimire.controller;

import com.clarimire.entity.SimulationConfig;
import com.clarimire.service.SimulationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation-configs")
@CrossOrigin(origins = "*")
public class SimulationConfigController {

    @Autowired
    private SimulationConfigService simulationConfigService;

    @GetMapping
    public List<SimulationConfig> getAll() {
        return simulationConfigService.getAllConfigs();
    }

    @GetMapping("/default")
    public SimulationConfig getDefault() {
        return simulationConfigService.getDefaultConfig();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SimulationConfig config) {
        SimulationConfig created = simulationConfigService.createConfig(config);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SimulationConfig config) {
        config.setId(id);
        simulationConfigService.updateConfig(config);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        simulationConfigService.deleteConfig(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<?> setDefault(@PathVariable Integer id) {
        simulationConfigService.setDefaultConfig(id);
        return ResponseEntity.ok(Map.of("message", "已设为默认配置"));
    }
}
