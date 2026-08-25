package com.clarimire.mapper;

import com.clarimire.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OperationLogMapper {
    List<OperationLog> selectList(@Param("username") String username,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);
    int insert(OperationLog operationLog);
    int deleteById(@Param("id") Integer id);
}
