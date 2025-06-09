package com.example.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.aiagent.entity.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具调用基础代理类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ToolCallAgent extends ReActAgent {
    // 可用的工具
    private ToolCallback[] toolCallbacks;

    // 工具调用的响应
    private ChatResponse chatResponse;

    // 工具调用的管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用Spring AI管理管理工具调用
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] toolCallbacks) {
        super();
        this.toolCallbacks = toolCallbacks;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
            .withProxyToolCalls(true)
            .build();
    }

    @Override
    public boolean think() {
        if (getNextPrompt() != null && !getNextPrompt().isEmpty()) {
            UserMessage message = new UserMessage(getNextPrompt());
            getMessages().add(message);
        }
        List<Message> messages = getMessages();
        Prompt prompt = new Prompt(messages, chatOptions);
        try {
            // 获取工具的响应
            ChatResponse chattedResponse = getChatClient().prompt(prompt)
                .system(getSystemPrompt())
                .tools(toolCallbacks)
                .call()
                .chatResponse();
            this.chatResponse = chattedResponse;
            AssistantMessage assistantMessage = chattedResponse.getResult().getOutput();
            // 输出提示信息
            String text = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info(getName() + "思考" + text);
            log.info(getName() + "调用了" + toolCalls.size() + "个工具");
            String collected = toolCalls.stream()
                .map(toolCall ->
                    String.format("工具名称：%s，工具参数：%s", toolCall.name(), toolCall.arguments())
                ).collect(Collectors.joining("\n"));
            log.info(collected);
            if (toolCalls.isEmpty()) {
                getMessages().add(assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("调用工具失败", e);
            getMessages().add(new AssistantMessage("处理时遇到错误" + e.getMessage()));
            return false;
        }
    }

    /**
     * 调用工具并收集结果
     *
     * @return
     */
    @Override
    public String act() {
        if (!chatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessages(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
        setMessages(toolExecutionResult.conversationHistory());
        ToolResponseMessage message = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String collect = message.getResponses().stream()
            .map(toolResponse -> "工具名称：" + toolResponse.name() + "，工具结果：" + toolResponse.responseData())
            .collect(Collectors.joining("\n"));
        boolean doTerminate = message.getResponses().stream()
            .anyMatch(toolResponse ->
                "doTerminate".equals(toolResponse.name()) ||
                    "TerminateTool_doTerminate".equals(toolResponse.name()) ||
                    toolResponse.responseData().contains("任务结束")
            );
        if (doTerminate) {
            setState(AgentState.FINISHED);
        }
        log.info(collect);
        return collect;
    }
}
