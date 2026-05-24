package com.clarimire.mapper;

import com.clarimire.entity.InspectionTask;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface InspectionTaskMapper {

    @Select("SELECT * FROM inspection_tasks WHERE id = #{id} LIMIT 1")
    InspectionTask findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM inspection_tasks WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "<if test='assigneeName != null'> AND assignee_name LIKE CONCAT('%', #{assigneeName}, '%') </if>" +
            "<if test='creatorId != null'> AND creator_id = #{creatorId} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<InspectionTask> findAll(@Param("status") String status,
                                 @Param("assigneeId") Integer assigneeId,
                                 @Param("assigneeName") String assigneeName,
                                 @Param("creatorId") Integer creatorId);

    @Select("SELECT * FROM inspection_tasks WHERE assignee_id = #{assigneeId} AND status = 'pending' ORDER BY created_at DESC")
    List<InspectionTask> findPendingByAssignee(@Param("assigneeId") Integer assigneeId);

    @Insert("INSERT INTO inspection_tasks (title, description, reservoir_name, latitude, longitude, status, " +
            "creator_id, creator_name, assignee_id, assignee_name, deadline, created_at, updated_at) " +
            "VALUES (#{title}, #{description}, #{reservoirName}, #{latitude}, #{longitude}, #{status}, " +
            "#{creatorId}, #{creatorName}, #{assigneeId}, #{assigneeName}, #{deadline}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InspectionTask task);

    @Update("UPDATE inspection_tasks SET title=#{title}, description=#{description}, reservoir_name=#{reservoirName}, " +
            "latitude=#{latitude}, longitude=#{longitude}, status=#{status}, assignee_id=#{assigneeId}, " +
            "assignee_name=#{assigneeName}, deadline=#{deadline}, updated_at=NOW() WHERE id=#{id}")
    void update(InspectionTask task);

    @Update("UPDATE inspection_tasks SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Delete("DELETE FROM inspection_tasks WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM inspection_tasks WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(*) FROM inspection_tasks WHERE assignee_id = #{assigneeId} AND status = 'pending'")
    int countPendingByAssignee(Integer assigneeId);
}
