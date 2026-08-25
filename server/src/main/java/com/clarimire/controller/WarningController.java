package com.clarimire.controller;

import com.clarimire.entity.WaterReservoir;
import com.clarimire.mapper.WaterReservoirMapper;
import com.clarimire.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warning")
public class WarningController {

    @Autowired
    private WarningService warningService;

    @Autowired
    private WaterReservoirMapper waterReservoirMapper;

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String reservoirName) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", warningService.getList(reservoirName));
        return result;
    }

    @GetMapping("/reservoirs")
    public Map<String, Object> reservoirs() {
        Map<String, Object> result = new HashMap<>();
        List<WaterReservoir> list = waterReservoirMapper.selectAll();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    @PostMapping("/evaluate")
    public Map<String, Object> evaluate(@RequestBody Map<String, Object> body) {
        String reservoirName = (String) body.get("reservoirName");
        Object val = body.get("waterLevel");
        BigDecimal waterLevel = val == null ? null : new BigDecimal(val.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", warningService.evaluateWaterLevel(reservoirName, waterLevel));
        return result;
    }

    @PostMapping("/evaluateEnv")
    public Map<String, Object> evaluateEnv(@RequestBody Map<String, Object> body) {
        String reservoirName = (String) body.get("reservoirName");
        String indicator = (String) body.get("indicator");
        Object val = body.get("value");
        BigDecimal value = val == null ? null : new BigDecimal(val.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", warningService.evaluateEnvironment(reservoirName, indicator, value));
        return result;
    }
}
