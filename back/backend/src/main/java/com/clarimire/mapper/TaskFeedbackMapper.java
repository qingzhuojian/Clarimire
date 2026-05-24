package com.clarimire.mapper;

import com.clarimire.entity.TaskFeedback;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TaskFeedbackMapper {

    @Select("SELECT * FROM task_feedbacks WHERE id = #{id} LIMIT 1")
    TaskFeedback findById(Integer id);

    @Select("SELECT * FROM task_feedbacks WHERE task_id = #{taskId} ORDER BY created_at ASC")
    List<TaskFeedback> findByTaskId(@Param("taskId") Integer taskId);

    @Insert("INSERT INTO task_feedbacks (task_id, content, photos, inspector, inspector_username, created_at) " +
            "VALUES (#{taskId}, #{content}, #{photos}, #{inspector}, #{inspectorUsername}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskFeedback feedback);

    @Delete("DELETE FROM task_feedbacks WHERE id = #{id}")
    void deleteById(Integer id);

    @Delete("DELETE FROM task_feedbacks WHERE task_id = #{taskId}")
    void deleteByTaskId(Integer taskId);

    @Select("SELECT COUNT(*) FROM task_feedbacks WHERE task_id = #{taskId}")
    int countByTaskId(Integer taskId);
}
