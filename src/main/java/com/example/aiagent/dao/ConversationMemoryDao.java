package com.example.aiagent.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.ConversationMemory;
import com.example.aiagent.mapper.ConversationMemoryMapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ConversationMemoryDao extends ServiceImpl<ConversationMemoryMapper, ConversationMemory> {

    public List<ConversationMemory> getMessages(String conversationId) {
        return this.lambdaQuery().
            eq(ConversationMemory::getConversationId, conversationId).list();
    }

    public boolean deleteByConversationId(String conversationId) {
        return this.lambdaUpdate()
            .eq(ConversationMemory::getConversationId, conversationId).remove();
    }
}
