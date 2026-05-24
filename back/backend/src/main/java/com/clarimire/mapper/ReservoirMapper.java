package com.clarimire.mapper;

import com.clarimire.entity.Reservoir;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReservoirMapper {
    @Select("SELECT * FROM reservoirs WHERE id = #{id} LIMIT 1")
    Reservoir findById(Integer id);

    @Select("SELECT * FROM reservoirs WHERE reservoir_name = #{name} LIMIT 1")
    Reservoir findByName(String name);

    @Select("SELECT * FROM reservoirs ORDER BY created_at DESC")
    List<Reservoir> findAll();

    @Insert("INSERT INTO reservoirs (reservoir_name, location, latitude, longitude, capacity, status, " +
            "construction_date, last_maintenance_date, created_at, updated_at) " +
            "VALUES (#{reservoirName}, #{location}, #{latitude}, #{longitude}, #{capacity}, #{status}, " +
            "#{constructionDate}, #{lastMaintenanceDate}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Reservoir reservoir);

    @Update("UPDATE reservoirs SET reservoir_name=#{reservoirName}, location=#{location}, " +
            "latitude=#{latitude}, longitude=#{longitude}, capacity=#{capacity}, status=#{status}, " +
            "construction_date=#{constructionDate}, last_maintenance_date=#{lastMaintenanceDate}, " +
            "updated_at=#{updatedAt} WHERE id=#{id}")
    void update(Reservoir reservoir);

    @Delete("DELETE FROM reservoirs WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM reservoirs")
    int count();
}
