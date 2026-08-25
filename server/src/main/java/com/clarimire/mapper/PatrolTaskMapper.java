package com.clarimire.mapper;

import com.clarimire.entity.PatrolTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PatrolTaskMapper {
    List<PatrolTask> findList(@Param("status") String status,
                              @Param("assigneeId") Integer assigneeId,
                              @Param("reservoirName") String reservoirName,
                              @Param("taskType") String taskType);

    PatrolTask findById(@Param("id") Integer id);

    int insert(PatrolTask task);

    int update(PatrolTask task);

    int deleteById(@Param("id") Integer id);

    int countByStatus(@Param("status") String status);
}
