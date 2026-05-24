package com.clarimire.controller;

import com.clarimire.entity.LayerConfig;
import com.clarimire.service.LayerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/layer-configs")
@CrossOrigin(origins = "*")
public class LayerConfigController {

    @Autowired
    private LayerConfigService layerConfigService;

    @GetMapping
    public List<LayerConfig> getAll() {
        return layerConfigService.getAllConfigs();
    }

    @PutMapping
    public ResponseEntity<?> updateConfigs(@RequestBody List<LayerConfig> configs) {
        layerConfigService.updateConfigs(configs);
        return ResponseEntity.ok(Map.of("message", "图层配置更新成功"));
    }

    @GetMapping("/types")
    public List<String> getLayerTypes() {
        return layerConfigService.getLayerTypes();
    }
}
