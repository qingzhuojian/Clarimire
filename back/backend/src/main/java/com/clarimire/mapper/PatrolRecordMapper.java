package com.clarimire.mapper;

import com.clarimire.entity.PatrolRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PatrolRecordMapper {
    @Select("SELECT * FROM patrol_records WHERE id = #{id} LIMIT 1")
    PatrolRecord findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM patrol_records WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='inspector != null'> AND (inspector LIKE CONCAT('%', #{inspector}, '%') OR assigned_inspector LIKE CONCAT('%', #{inspector}, '%')) </if>" +
            "<if test='startDate != null'> AND date >= #{startDate} </if>" +
            "<if test='endDate != null'> AND date &lt;= #{endDate} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<PatrolRecord> findAll(@Param("status") String status,
                               @Param("inspector") String inspector,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    @Insert("INSERT INTO patrol_records (date, time, reservoir_name, latitude, longitude, address, " +
            "inspector, inspector_username, status, has_issue, issue_type, issue_severity, description, " +
            "has_photo, photo_urls, reporter_name, reporter_role, assigned_inspector, assignment_note, " +
            "assignment_time, situation_description, processing_result, completion_time, created_at, updated_at) " +
            "VALUES (#{date}, #{time}, #{reservoirName}, #{latitude}, #{longitude}, #{address}, " +
            "#{inspector}, #{inspectorUsername}, #{status}, #{hasIssue}, #{issueType}, #{issueSeverity}, #{description}, " +
            "#{hasPhoto}, #{photoUrls}, #{reporterName}, #{reporterRole}, #{assignedInspector}, #{assignmentNote}, " +
            "#{assignmentTime}, #{situationDescription}, #{processingResult}, #{completionTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatrolRecord record);

    @Update("UPDATE patrol_records SET date=#{date}, time=#{time}, reservoir_name=#{reservoirName}, " +
            "latitude=#{latitude}, longitude=#{longitude}, address=#{address}, inspector=#{inspector}, " +
            "inspector_username=#{inspectorUsername}, status=#{status}, has_issue=#{hasIssue}, " +
            "issue_type=#{issueType}, issue_severity=#{issueSeverity}, description=#{description}, " +
            "has_photo=#{hasPhoto}, photo_urls=#{photoUrls}, reporter_name=#{reporterName}, " +
            "reporter_role=#{reporterRole}, assigned_inspector=#{assignedInspector}, " +
            "assignment_note=#{assignmentNote}, assignment_time=#{assignmentTime}, " +
            "situation_description=#{situationDescription}, processing_result=#{processingResult}, " +
            "completion_time=#{completionTime}, updated_at=NOW() WHERE id=#{id}")
    void update(PatrolRecord record);

    @Delete("DELETE FROM patrol_records WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM patrol_records WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(*) FROM patrol_records WHERE DATE(created_at) = CURDATE() AND status = #{status}")
    int countTodayByStatus(String status);

    @Select("SELECT COUNT(DISTINCT inspector) FROM patrol_records WHERE DATE(created_at) = CURDATE()")
    int countActiveInspectorsToday();
}
