package com.clarimire.service.impl;

import com.clarimire.entity.LayerConfig;
import com.clarimire.mapper.LayerConfigMapper;
import com.clarimire.service.LayerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class LayerConfigServiceImpl implements LayerConfigService {

    @Autowired
    private LayerConfigMapper layerConfigMapper;

    private static final List<String> DEFAULT_LAYER_TYPES = Arrays.asList(
        "admin_boundaries",  // 行政区划
        "rivers",            // 河流
        "reservoirs",        // 水库
        "monitor_points",    // 监测点
        "residents"          // 居民点
    );

    @Override
    public List<LayerConfig> getAllConfigs() {
        List<LayerConfig> configs = layerConfigMapper.findAll();
        if (configs.isEmpty()) {
            // 返回默认配置
            configs = createDefaultConfigs();
        }
        return configs;
    }

    @Override
    public void updateConfigs(List<LayerConfig> configs) {
        for (LayerConfig config : configs) {
            config.setUpdatedAt(LocalDateTime.now());
            layerConfigMapper.update(config);
        }
    }

    @Override
    public List<String> getLayerTypes() {
        return DEFAULT_LAYER_TYPES;
    }

    private List<LayerConfig> createDefaultConfigs() {
        String[][] defaultLayers = {
            {"admin_boundaries", "行政区划", "#FF6B6B", "0.8", "true", "polygon"},
            {"rivers", "河流", "#4ECDC4", "0.8", "true", "line"},
            {"reservoirs", "水库", "#45B7D1", "0.9", "true", "point"},
            {"monitor_points", "监测点", "#96CEB4", "1.0", "true", "point"},
            {"residents", "居民点", "#FFEAA7", "0.8", "true", "point"}
        };

        for (String[] layer : defaultLayers) {
            LayerConfig config = new LayerConfig();
            config.setLayerType(layer[0]);
            config.setLayerName(layer[1]);
            config.setColor(layer[2]);
            config.setOpacity(java.math.BigDecimal.valueOf(Double.parseDouble(layer[3])));
            config.setVisible(Boolean.parseBoolean(layer[4]));
            config.setIcon(layer[5]);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            layerConfigMapper.insert(config);
        }

        return layerConfigMapper.findAll();
    }
}
