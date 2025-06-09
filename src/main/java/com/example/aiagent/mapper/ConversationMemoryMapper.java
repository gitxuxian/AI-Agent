package com.example.aiagent.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiagent.entity.ConversationMemory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author hy
 * @description 针对表【conversation_memory】的数据库操作Mapper
 * @createDate 2025-06-07 18:13:35
 */
@Mapper
public interface ConversationMemoryMapper extends BaseMapper<ConversationMemory> {

}




