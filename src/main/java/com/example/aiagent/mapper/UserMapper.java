package com.example.aiagent.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiagent.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hy
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @createDate 2025-06-09 15:26:5
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




