package com.clarimire.mapper;

import com.clarimire.entity.WaterSituation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WaterSituationMapper {
    List<WaterSituation> selectList(@Param("reservoirName") String reservoirName, 
                                     @Param("startDate") String startDate, 
                                     @Param("endDate") String endDate);
    WaterSituation selectById(@Param("id") Integer id);
    int insert(WaterSituation waterSituation);
    int insertBatch(List<WaterSituation> list);
    int update(WaterSituation waterSituation);
    int deleteById(@Param("id") Integer id);
    int deleteBatch(List<Integer> ids);
    List<String> selectDistinctReservoirs();
}
