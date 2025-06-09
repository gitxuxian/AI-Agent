package com.example.aiagent.chatmemory;

import cn.hutool.core.collection.CollUtil;
import com.example.aiagent.dao.ConversationMemoryDao;
import com.example.aiagent.entity.ConversationMemory;
import com.example.aiagent.entity.MessageTypeEnum;
import com.google.gson.Gson;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆持久化到数据库中
 */
@Component
public class MysqlChatMemory implements ChatMemory {

    private final ConversationMemoryDao conversationMemoryDao;

    public MysqlChatMemory(ConversationMemoryDao conversationMemoryDao) {
        this.conversationMemoryDao = conversationMemoryDao;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Gson gson = new Gson();
        List<ConversationMemory> conversationMemories = messages.stream().map(message -> {
            String messageType = message.getMessageType().getValue();
            String json = gson.toJson(message);
            return ConversationMemory.builder().conversationId(conversationId).type(messageType).memory(json).build();
        }).toList();
        conversationMemoryDao.saveBatch(conversationMemories);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<ConversationMemory> memoryDaoMessages = conversationMemoryDao.getMessages(conversationId);
        if (CollUtil.isEmpty(memoryDaoMessages)) {
            return List.of();
        }
        return memoryDaoMessages.stream()
            .skip(Math.max(0, memoryDaoMessages.size() - lastN))
            .map(this::getMessage)
            .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        conversationMemoryDao.deleteByConversationId(conversationId);
    }

    private Message getMessage(ConversationMemory conversationMemory) {
        String memory = conversationMemory.getMemory();
        Gson gson = new Gson();
        return (Message) gson.fromJson(memory, MessageTypeEnum.fromValue(conversationMemory.getType()).getClazz());
    }

}
