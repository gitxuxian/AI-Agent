package com.example.aiagent.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.aiagent.agent.Manus;
import com.example.aiagent.app.LoveApp;
import com.example.aiagent.common.BaseResponse;
import com.example.aiagent.entity.User;
import com.example.aiagent.entity.vo.LoginUserVO;
import com.example.aiagent.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * @author hy
 * @description AI相关接口
 * @createDate 2025-06-09 15:26:57
 */
@RestController
@RequestMapping("/ai")
public class AIcontroller {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    /**
     * love App 流式输出
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/app/chat/sse")
    @SaCheckLogin
    public Flux<ServerSentEvent<String>> chatWithAppSEE(@RequestParam("message") String message, @RequestParam("chatId") String chatId) {
        return Flux.concat(
            loveApp.doChatByStream(chatId, message)
                .map(chunck -> ServerSentEvent.<String>builder().data(chunck).build()),
            Flux.just(ServerSentEvent.<String>builder().data("[DONE]").build())
        );
    }

    /**
     * mauns流式输出
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    @SaCheckLogin
    public SseEmitter chatWithManus(@RequestParam("message") String message) {
        Manus manus = new Manus(allTools, dashscopeChatModel);
        return manus.runWithStream(message);
    }

}
