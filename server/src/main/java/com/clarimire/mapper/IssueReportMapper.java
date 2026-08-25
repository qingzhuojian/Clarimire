package com.clarimire.mapper;

import com.clarimire.entity.IssueReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IssueReportMapper {
    List<IssueReport> findList(@Param("status") String status,
                               @Param("issueType") String issueType,
                               @Param("reporterId") Integer reporterId);

    IssueReport findById(@Param("id") Integer id);

    int insert(IssueReport report);

    int update(IssueReport report);

    int countByStatus(@Param("status") String status);
}
