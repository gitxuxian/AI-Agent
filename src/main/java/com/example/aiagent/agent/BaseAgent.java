package com.example.aiagent.agent;

import com.example.aiagent.entity.AgentState;
import com.example.aiagent.exception.AgentException;
import com.example.aiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Agent基类
 */

@Data
@Slf4j
public abstract class BaseAgent {
    //核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxStep = 20;

    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // 消息记忆
    private List<Message> messages = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new AgentException(ErrorCode.AGENT_ERROR, "Cannot run agent when it is not in idle state");
        }
        if (StringUtil.isEmpty(userPrompt)) {
            throw new AgentException(ErrorCode.AGENT_ERROR, "User prompt cannot be empty");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录上下位
        messages.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int numberStep = i + 1;
                currentStep = numberStep;
                log.info("Step {}:", numberStep);
                String stepResult = step();
                String result = "Step" + numberStep + ":" + stepResult;
                log.info(result);
                results.add(result);
                // 额外的终止检查
                if (state == AgentState.FINISHED) {
                    log.info("代理已完成任务，提前退出循环");
                    break;
                }
                // 检查步骤结果中是否包含终止信号
                if (stepResult != null && stepResult.contains("TERMINATE_SIGNAL")) {
                    log.info("在步骤结果中检测到终止信号");
                    state = AgentState.FINISHED;
                    break;
                }
            }
            if (currentStep > maxStep) {
                state = AgentState.FINISHED;
                results.add("Agent has reached the maximum step limit" + maxStep);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Agent error", e);
            return "Agent error: " + e.getMessage();
        } finally {
            this.clean();
        }
    }

    /**
     * 流式方法
     *
     * @param userPrompt
     * @return
     */
    public SseEmitter runWithStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);

        //创建异步任务
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误无法从状态运行代理" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StringUtil.isEmpty(userPrompt)) {
                    sseEmitter.send("错误,用户提示不能为空");
                    sseEmitter.complete();
                    return;
                }
                // 更改状态
                state = AgentState.RUNNING;
                // 记录上下位
                messages.add(new UserMessage(userPrompt));
                // 保存结果列表
//                List<String> results = new ArrayList<>();
                try {
                    for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                        int numberStep = i + 1;
                        currentStep = numberStep;
                        log.info("Step {}:", numberStep);
                        String stepResult = step();
                        String result = "Step" + numberStep + ":" + stepResult;
                        log.info(result);
                        sseEmitter.send(result);
                        // 额外的终止检查
                        if (state == AgentState.FINISHED) {
                            sseEmitter.send("代理已完成任务，提前退出循环");
                            log.info("代理已完成任务，提前退出循环");
                            break;
                        }
                        // 检查步骤结果中是否包含终止信号
                        if (stepResult != null && stepResult.contains("TERMINATE_SIGNAL")) {
                            sseEmitter.send("在步骤结果中检测到终止信号");
                            log.info("在步骤结果中检测到终止信号");
                            state = AgentState.FINISHED;
                            break;
                        }
                    }
                    if (currentStep > maxStep) {
                        state = AgentState.FINISHED;
                        sseEmitter.send("执行达到最大步骤" + maxStep);
                    }
                    // 正常完成
                    sseEmitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    sseEmitter.send("执行错误" + e.getMessage());
                    sseEmitter.complete();
                } finally {
                    this.clean();
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        });
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.clean();
            log.error("执行超时");
        });
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.clean();
            log.info("执行完成");
        });
        return sseEmitter;
    }

    /**
     * 单步执行步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void clean() {

    }
}
