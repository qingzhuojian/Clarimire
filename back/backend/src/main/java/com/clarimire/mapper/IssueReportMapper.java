package com.clarimire.mapper;

import com.clarimire.entity.IssueReport;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface IssueReportMapper {

    @Select("SELECT * FROM issue_reports WHERE id = #{id} LIMIT 1")
    IssueReport findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM issue_reports WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='severity != null'> AND severity = #{severity} </if>" +
            "<if test='reporterUsername != null'> AND reporter_username = #{reporterUsername} </if>" +
            "<if test='assignedInspector != null'> AND assigned_inspector = #{assignedInspector} </if>" +
            "<if test='startDate != null'> AND DATE(created_at) >= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(created_at) &lt;= #{endDate} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<IssueReport> findAll(@Param("status") String status,
                               @Param("severity") String severity,
                               @Param("reporterUsername") String reporterUsername,
                               @Param("assignedInspector") String assignedInspector,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    @Select("SELECT * FROM issue_reports WHERE reporter_username = #{reporterUsername} ORDER BY created_at DESC")
    List<IssueReport> findByReporter(@Param("reporterUsername") String reporterUsername);

    @Select("SELECT * FROM issue_reports WHERE status = 'pending' ORDER BY severity DESC, created_at DESC")
    List<IssueReport> findPending();

    @Insert("INSERT INTO issue_reports (reservoir_name, description, severity, notes, photos, latitude, longitude, " +
            "address, reporter_name, reporter_role, reporter_username, status, created_at, updated_at) " +
            "VALUES (#{reservoirName}, #{description}, #{severity}, #{notes}, #{photos}, #{latitude}, #{longitude}, " +
            "#{address}, #{reporterName}, #{reporterRole}, #{reporterUsername}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IssueReport report);

    @Update("UPDATE issue_reports SET reservoir_name=#{reservoirName}, description=#{description}, severity=#{severity}, " +
            "notes=#{notes}, photos=#{photos}, latitude=#{latitude}, longitude=#{longitude}, address=#{address}, " +
            "status=#{status}, assigned_inspector=#{assignedInspector}, assignment_note=#{assignmentNote}, " +
            "assignment_time=#{assignmentTime}, processing_result=#{processingResult}, " +
            "completion_time=#{completionTime}, updated_at=NOW() WHERE id=#{id}")
    void update(IssueReport report);

    @Update("UPDATE issue_reports SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Update("UPDATE issue_reports SET assigned_inspector=#{assignedInspector}, assignment_note=#{assignmentNote}, " +
            "assignment_time=NOW(), status='processing', updated_at=NOW() WHERE id=#{id}")
    void assign(@Param("id") Integer id, @Param("assignedInspector") String assignedInspector,
                @Param("assignmentNote") String assignmentNote);

    @Update("UPDATE issue_reports SET processing_result=#{processingResult}, completion_time=NOW(), " +
            "status='completed', updated_at=NOW() WHERE id=#{id}")
    void complete(@Param("id") Integer id, @Param("processingResult") String processingResult);

    @Delete("DELETE FROM issue_reports WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM issue_reports WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(*) FROM issue_reports WHERE DATE(created_at) = CURDATE() AND status = #{status}")
    int countTodayByStatus(String status);
}
