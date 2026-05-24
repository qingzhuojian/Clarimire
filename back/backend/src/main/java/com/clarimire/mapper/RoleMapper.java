package com.clarimire.mapper;

import com.clarimire.entity.Role;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("SELECT * FROM roles ORDER BY id ASC")
    List<Role> findAll();

    @Select("SELECT * FROM roles WHERE id = #{id} LIMIT 1")
    Role findById(Integer id);

    @Select("SELECT * FROM roles WHERE role_key = #{roleKey} LIMIT 1")
    Role findByRoleKey(String roleKey);

    @Insert("INSERT INTO roles (role_name, role_key, description, permissions, created_at, updated_at) " +
            "VALUES (#{roleName}, #{roleKey}, #{description}, #{permissions}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Role role);

    @Update("UPDATE roles SET role_name=#{roleName}, role_key=#{roleKey}, description=#{description}, " +
            "permissions=#{permissions}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(Role role);

    @Delete("DELETE FROM roles WHERE id = #{id}")
    void deleteById(Integer id);
}
