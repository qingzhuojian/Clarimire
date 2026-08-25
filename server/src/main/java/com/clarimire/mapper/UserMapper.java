package com.clarimire.mapper;

import com.clarimire.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Integer id);
    List<User> findAll(@Param("role") String role);
    int insert(User user);
    int update(User user);
    int deleteById(@Param("id") Integer id);
}
