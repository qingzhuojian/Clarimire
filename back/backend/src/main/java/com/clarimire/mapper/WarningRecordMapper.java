package com.clarimire.mapper;

import com.clarimire.entity.WarningRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WarningRecordMapper {
    @Select("SELECT * FROM warning_records WHERE id = #{id} LIMIT 1")
    WarningRecord findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM warning_records WHERE 1=1 " +
            "<if test='level != null'> AND warning_level = #{level} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='startDate != null'> AND DATE(created_at) >= #{startDate} </if>" +
            "<if test='endDate != null'> AND DATE(created_at) &lt;= #{endDate} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<WarningRecord> findAll(@Param("level") String level,
                                @Param("status") String status,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate);

    @Select("SELECT * FROM warning_records ORDER BY created_at DESC LIMIT #{limit}")
    List<WarningRecord> findLatest(@Param("limit") int limit);

    @Insert("INSERT INTO warning_records (warning_type, warning_level, reservoir_id, reservoir_name, " +
            "latitude, longitude, description, indicator_value, threshold_value, status, created_at, updated_at) " +
            "VALUES (#{warningType}, #{warningLevel}, #{reservoirId}, #{reservoirName}, " +
            "#{latitude}, #{longitude}, #{description}, #{indicatorValue}, #{thresholdValue}, #{status}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WarningRecord warning);

    @Update("UPDATE warning_records SET warning_type=#{warningType}, warning_level=#{warningLevel}, " +
            "reservoir_id=#{reservoirId}, reservoir_name=#{reservoirName}, latitude=#{latitude}, " +
            "longitude=#{longitude}, description=#{description}, indicator_value=#{indicatorValue}, " +
            "threshold_value=#{thresholdValue}, status=#{status}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(WarningRecord warning);

    @Delete("DELETE FROM warning_records WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM warning_records WHERE DATE(created_at) = CURDATE()")
    int countTodayWarnings();

    @Select("SELECT COUNT(*) FROM warning_records WHERE DATE(created_at) = CURDATE() AND status = #{status}")
    int countTodayByStatus(String status);

    @Select("SELECT COUNT(*) FROM warning_records WHERE status = #{status}")
    int countByStatus(String status);
}
