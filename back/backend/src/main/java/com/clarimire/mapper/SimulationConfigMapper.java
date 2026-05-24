package com.clarimire.mapper;

import com.clarimire.entity.SimulationConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SimulationConfigMapper {
    @Select("SELECT * FROM simulation_configs ORDER BY is_default DESC, id DESC")
    List<SimulationConfig> findAll();

    @Select("SELECT * FROM simulation_configs WHERE id = #{id} LIMIT 1")
    SimulationConfig findById(Integer id);

    @Select("SELECT * FROM simulation_configs WHERE is_default = TRUE LIMIT 1")
    SimulationConfig findDefault();

    @Insert("INSERT INTO simulation_configs (config_name, diffusion_radius, decay_coefficient, wind_speed, " +
            "wind_direction, water_flow_rate, simulation_duration, is_default, created_at, updated_at) " +
            "VALUES (#{configName}, #{diffusionRadius}, #{decayCoefficient}, #{windSpeed}, " +
            "#{windDirection}, #{waterFlowRate}, #{simulationDuration}, #{isDefault}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SimulationConfig config);

    @Update("UPDATE simulation_configs SET config_name=#{configName}, diffusion_radius=#{diffusionRadius}, " +
            "decay_coefficient=#{decayCoefficient}, wind_speed=#{windSpeed}, wind_direction=#{windDirection}, " +
            "water_flow_rate=#{waterFlowRate}, simulation_duration=#{simulationDuration}, " +
            "is_default=#{isDefault}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(SimulationConfig config);

    @Delete("DELETE FROM simulation_configs WHERE id = #{id}")
    void deleteById(Integer id);

    @Update("UPDATE simulation_configs SET is_default = FALSE")
    void clearAllDefaults();

    @Update("UPDATE simulation_configs SET is_default = TRUE WHERE id = #{id}")
    void setDefault(Integer id);
}
