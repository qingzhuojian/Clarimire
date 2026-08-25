package com.clarimire.mapper;

import com.clarimire.entity.PatrolRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PatrolRecordMapper {
    List<PatrolRecord> findList(@Param("userId") Integer userId,
                                @Param("reservoirName") String reservoirName,
                                @Param("locationZone") String locationZone,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate);

    PatrolRecord findById(@Param("id") Integer id);

    int insert(PatrolRecord record);

    int countTodayByUser(@Param("userId") Integer userId);

    int countByTaskId(@Param("taskId") Integer taskId);
}
