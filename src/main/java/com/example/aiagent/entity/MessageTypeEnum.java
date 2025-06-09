package com.example.aiagent.entity;

import lombok.Getter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2025/4/28
 * @description
 */
@Getter
public enum MessageTypeEnum {


    USER("user", UserMessage.class),


    ASSISTANT("assistant", AssistantMessage.class),


    SYSTEM("system", SystemMessage.class),


    TOOL("tool", ToolResponseMessage.class);

    private final String value;

    private final Class<?> clazz;

    MessageTypeEnum(String value, Class<?> clazz) {
        this.value = value;
        this.clazz = clazz;
    }

    public static MessageTypeEnum fromValue(String value) {
        for (MessageTypeEnum messageType : MessageTypeEnum.values()) {
            if (messageType.getValue().equals(value)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + value);
    }

}

