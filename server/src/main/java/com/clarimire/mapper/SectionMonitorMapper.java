package com.clarimire.mapper;

import com.clarimire.entity.SectionMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SectionMonitorMapper {
    List<SectionMonitor> selectList(@Param("reservoirName") String reservoirName,
                                    @Param("year") Integer year,
                                    @Param("month") Integer month);
    SectionMonitor selectById(@Param("id") Integer id);
    int insert(SectionMonitor sectionMonitor);
    int insertBatch(List<SectionMonitor> list);
    int update(SectionMonitor sectionMonitor);
    int deleteById(@Param("id") Integer id);
    int deleteBatch(List<Integer> ids);
    List<String> selectDistinctReservoirs();
    List<Integer> selectDistinctYears();
}
