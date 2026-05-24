package com.clarimire.mapper;

import com.clarimire.entity.WarningThreshold;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WarningThresholdMapper {
    @Select("SELECT * FROM warning_thresholds ORDER BY id DESC LIMIT 1")
    WarningThreshold findLatest();

    @Select("SELECT * FROM warning_thresholds ORDER BY id DESC LIMIT 1")
    WarningThreshold findCurrent();

    @Insert("INSERT INTO warning_thresholds (cod_threshold, ammonia_nitrogen_threshold, total_phosphorus_threshold, " +
            "total_nitrogen_threshold, permanganate_threshold, flood_limit_water_level, created_at, updated_at) " +
            "VALUES (#{codThreshold}, #{ammoniaNitrogenThreshold}, #{totalPhosphorusThreshold}, " +
            "#{totalNitrogenThreshold}, #{permanganateThreshold}, #{floodLimitWaterLevel}, " +
            "NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WarningThreshold threshold);

    @Update("UPDATE warning_thresholds SET cod_threshold=#{codThreshold}, " +
            "ammonia_nitrogen_threshold=#{ammoniaNitrogenThreshold}, " +
            "total_phosphorus_threshold=#{totalPhosphorusThreshold}, " +
            "total_nitrogen_threshold=#{totalNitrogenThreshold}, " +
            "permanganate_threshold=#{permanganateThreshold}, " +
            "flood_limit_water_level=#{floodLimitWaterLevel}, " +
            "updated_at=NOW() WHERE id=#{id}")
    void update(WarningThreshold threshold);

    default void insertOrUpdate(WarningThreshold threshold) {
        WarningThreshold existing = findLatest();
        if (existing != null) {
            threshold.setId(existing.getId());
            update(threshold);
        } else {
            insert(threshold);
        }
    }
}
