package com.clarimire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*")
public class MapController {

    /**
     * 获取地图要素的GeoJSON数据
     * 支持类型: reservoirs, monitor_points, rivers, admin_boundaries, residents
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getFeatures(@RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        
        // GeoJSON结构
        Map<String, Object> geojson = new HashMap<>();
        geojson.put("type", "FeatureCollection");
        geojson.put("features", new ArrayList<>());
        
        result.put("type", type != null ? type : "all");
        result.put("data", geojson);
        result.put("message", "获取地图要素成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取水库点位数据
     */
    @GetMapping("/features/reservoirs")
    public ResponseEntity<Map<String, Object>> getReservoirs() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> features = new ArrayList<>();
        
        // 示例数据 - 实际应从数据库获取
        String[][] sampleReservoirs = {
            {"青山水库", "120.139558", "30.246815", "9060万立方米", "normal"},
            {"秀水湖水库", "120.627423", "31.298441", "4200万立方米", "normal"},
            {"碧水源水库", "121.092345", "31.128547", "2800万立方米", "normal"}
        };
        
        for (String[] r : sampleReservoirs) {
            Map<String, Object> feature = createPointFeature(
                Double.parseDouble(r[1]), 
                Double.parseDouble(r[2]),
                r[0],
                Map.of(
                    "name", r[0],
                    "capacity", r[3],
                    "status", r[4],
                    "type", "reservoir"
                )
            );
            features.add(feature);
        }
        
        Map<String, Object> geojson = new HashMap<>();
        geojson.put("type", "FeatureCollection");
        geojson.put("features", features);
        
        result.put("data", geojson);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取监测点数据
     */
    @GetMapping("/features/monitor-points")
    public ResponseEntity<Map<String, Object>> getMonitorPoints(@RequestParam(required = false) String reservoirName) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> features = new ArrayList<>();
        
        // 示例数据
        String[][] samplePoints = {
            {"青山监测点1", "青山水库", "120.140000", "30.247000", "1.2", "6.5", "25"},
            {"青山监测点2", "青山水库", "120.138000", "30.246000", "1.1", "6.8", "22"},
            {"秀水监测点1", "秀水湖水库", "120.628000", "31.299000", "0.8", "5.5", "18"}
        };
        
        for (String[] p : samplePoints) {
            if (reservoirName == null || reservoirName.equals(p[1])) {
                Map<String, Object> feature = createPointFeature(
                    Double.parseDouble(p[2]), 
                    Double.parseDouble(p[3]),
                    p[0],
                    Map.of(
                        "name", p[0],
                        "reservoir", p[1],
                        "ammoniaNitrogen", p[4],
                        "permanganate", p[5],
                        "cod", p[6],
                        "type", "monitor_point"
                    )
                );
                features.add(feature);
            }
        }
        
        Map<String, Object> geojson = new HashMap<>();
        geojson.put("type", "FeatureCollection");
        geojson.put("features", features);
        
        result.put("data", geojson);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建GeoJSON点要素
     */
    private Map<String, Object> createPointFeature(double lng, double lat, String name, Map<String, Object> properties) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");
        
        // 几何
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", Arrays.asList(lng, lat));
        feature.put("geometry", geometry);
        
        // 属性
        Map<String, Object> props = new HashMap<>(properties);
        props.put("name", name);
        feature.put("properties", props);
        
        return feature;
    }
}
