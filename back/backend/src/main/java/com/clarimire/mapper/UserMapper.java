package com.clarimire.mapper;

import com.clarimire.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id} LIMIT 1")
    User findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM users WHERE 1=1 " +
            "<if test='username != null'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='role != null'> AND role = #{role} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<User> findAll(@Param("username") String username,
                       @Param("role") String role,
                       @Param("status") String status);

    @Insert("INSERT INTO users (username, password, real_name, role, phone, status, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{realName}, #{role}, #{phone}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE users SET username=#{username}, password=#{password}, real_name=#{realName}, " +
            "role=#{role}, phone=#{phone}, status=#{status}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM users WHERE role = 'inspector' AND status = 1")
    int countActiveInspectors();

    @Select("SELECT COUNT(*) FROM users WHERE role = 'inspector' AND status = 1 " +
            "AND updated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    int countTodayActiveInspectors();
}
