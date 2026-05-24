package com.clarimire.service;

import com.clarimire.entity.SimulationConfig;
import java.util.List;

public interface SimulationConfigService {
    List<SimulationConfig> getAllConfigs();
    SimulationConfig getDefaultConfig();
    SimulationConfig getConfigById(Integer id);
    SimulationConfig createConfig(SimulationConfig config);
    void updateConfig(SimulationConfig config);
    void deleteConfig(Integer id);
    void setDefaultConfig(Integer id);
}
