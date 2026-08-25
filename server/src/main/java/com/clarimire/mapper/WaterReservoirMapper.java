package com.clarimire.mapper;

import com.clarimire.entity.WaterReservoir;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WaterReservoirMapper {
    List<WaterReservoir> selectAll();
    WaterReservoir selectById(@Param("id") Integer id);
    int insert(WaterReservoir waterReservoir);
    int update(WaterReservoir waterReservoir);
    int deleteById(@Param("id") Integer id);
}
