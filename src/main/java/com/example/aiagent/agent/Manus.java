package com.example.aiagent.agent;

import com.example.aiagent.advisors.SimpleLoggerAdvisors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class Manus extends ToolCallAgent {

    public Manus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("Manus");
        String SYSTEM_PROMPT = """  
            You are Manus, an all-capable AI assistant, aimed at solving any task presented by the user.  
            You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
            """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
            Based on user needs, proactively select the most appropriate tool or combination of tools.  
            For complex tasks, you can break down the problem and use different tools step by step to solve it.  
            After using each tool, clearly explain the execution results and suggest the next steps.  
            If you want to stop the interaction at any point, use the `terminate` tool/function call.  
            """;
        this.setNextPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(20);
        // 初始化客户端  
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
            .defaultAdvisors(new SimpleLoggerAdvisors())
            .build();
        this.setChatClient(chatClient);
    }

}
