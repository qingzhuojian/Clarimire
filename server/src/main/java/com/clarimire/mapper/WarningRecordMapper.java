package com.clarimire.mapper;

import com.clarimire.entity.WarningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WarningRecordMapper {
    List<WarningRecord> selectList(@Param("reservoirName") String reservoirName);

    int insert(WarningRecord record);
}
