package com.clarimire.mapper;

import com.clarimire.entity.LayerConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LayerConfigMapper {
    @Select("SELECT * FROM layer_configs ORDER BY id ASC")
    List<LayerConfig> findAll();

    @Select("SELECT * FROM layer_configs WHERE layer_type = #{layerType} LIMIT 1")
    LayerConfig findByType(String layerType);

    @Insert("INSERT INTO layer_configs (layer_type, layer_name, color, opacity, visible, icon, created_at, updated_at) " +
            "VALUES (#{layerType}, #{layerName}, #{color}, #{opacity}, #{visible}, #{icon}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LayerConfig config);

    @Update("UPDATE layer_configs SET layer_type=#{layerType}, layer_name=#{layerName}, color=#{color}, " +
            "opacity=#{opacity}, visible=#{visible}, icon=#{icon}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(LayerConfig config);
}
