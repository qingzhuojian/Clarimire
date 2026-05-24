package com.clarimire.service;

import com.clarimire.entity.LayerConfig;
import java.util.List;

public interface LayerConfigService {
    List<LayerConfig> getAllConfigs();
    void updateConfigs(List<LayerConfig> configs);
    List<String> getLayerTypes();
}
