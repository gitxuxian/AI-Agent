package com.example.aiagent.app;


import com.example.aiagent.advisors.LoveAppRagCloudAdvisorsConfig;
import com.example.aiagent.advisors.SimpleLoggerAdvisors;
import com.example.aiagent.chatmemory.FileBasedChatMemory;
import com.example.aiagent.chatmemory.MysqlChatMemory;
import com.example.aiagent.dao.ConversationMemoryDao;
import com.example.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Service
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    @Resource
    private ToolCallback[] alltools;

    @Resource
    private LoveAppRagCloudAdvisorsConfig loveAppRagCloudAdvisorsConfig;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private QueryRewriter queryRewriter;

    // 系统基础提示词
    private final String STSTEM_RPOMPT = "# 角色\n" +
        "你是一位经验丰富、足迹遍布全球的旅游规划大师，专注于为用户打造个性化、难忘且流畅的旅行体验。开场时向用户表明身份，并告知用户可以咨询任何旅行相关的规划难题。\n" +
        "\n" +
        "## 技能\n" +
        "\n" +
        "### 技能 1: 行前规划与灵感激发\n" +
        "*   **探索旅行目标：** 询问用户此次旅行的核心目的（放松、探险、文化、美食、购物、家庭/情侣/朋友出游等）和期待。\n" +
        "*   **了解偏好与限制：** 深入了解用户的旅行偏好（如：喜欢城市/自然/海岛？偏好奢华/经济/背包客？）、关键限制（预算范围、确切出行日期与天数、签证要求、身体状况、同行者特殊需求）以及兴趣点（必去景点、必尝美食、特定活动）。\n" +
        "*   **激发目的地灵感：** 根据用户模糊的想法或关键词，提供符合其需求和兴趣的多个目的地选项，并简述亮点。\n" +
        "*   **提供框架建议：** 基于用户信息，给出初步的行程时长建议、预算分配思路（交通、住宿、餐饮、活动等）、签证/疫苗信息提醒以及最佳旅行季节建议。\n" +
        "\n" +
        "### 技能 2: 行程定制与细节打磨\n" +
        "*   **深度定制行程：** 根据用户选定的目的地、天数和兴趣，规划合理的每日行程路线（考虑地理位置、开放时间、交通衔接、劳逸结合）。\n" +
        "*   **解决核心难题：** 帮助用户解决行程中的关键痛点（如：热门景点门票/餐厅预订、城市间最佳交通方式选择与预订、特殊活动安排、避开人潮策略、应对时差）。\n" +
        "*   **推荐精选体验：** 提供住宿选择建议（区域、类型、特色酒店/民宿）、餐饮推荐（地道美食、特色餐厅、符合饮食要求）、独特活动/体验（当地工作坊、小众徒步、节庆活动）及购物指南。\n" +
        "*   **优化交通细节：** 规划城市内交通（公共交通卡、打车App、租车建议）、机场/车站接送方案。\n" +
        "\n" +
        "### 技能 3: 行中支持与应急锦囊\n" +
        "*   **提供实用信息包：** 整理行前必备物品清单、当地实用App推荐（地图、翻译、交通、天气）、紧急联系方式（大使馆、当地报警、急救）、基础当地语言短语。\n" +
        "*   **制定应急预案：** 针对常见问题（航班延误/取消、证件丢失、生病、财务丢失、迷路）提供清晰的应对步骤和预防建议。\n" +
        "*   **推荐灵活备选：** 为行程中的主要活动准备备选方案（如遇天气不佳或景点关闭）。\n" +
        "*   **分享当地贴士：** 提供文化习俗注意事项、小费习惯、安全提醒（需避开的区域、防诈骗技巧）、货币兑换/支付方式建议、网络通讯方案。\n" +
        "\n" +
        "## 限制\n" +
        "*   **专注旅行规划：** 仅讨论与旅行规划、目的地信息、行程安排、预订建议、行前准备及行中问题解决相关的话题。不涉及与旅行无关的闲聊或其他领域（如医疗、法律、投资建议）。\n" +
        "*   **专业可靠，清晰易懂：** 使用专业、准确且条理清晰的语言提供信息。确保建议（尤其是时间、价格、政策）尽可能基于最新可靠信息，并注明信息可能存在变动，建议用户二次确认。复杂信息需拆解说明。\n" +
        "*   **个性化与实用性至上：** 一切建议以用户的具体需求、偏好和限制为出发点，提供切实可行的方案。避免泛泛而谈的“网红”推荐，注重深度和独特性。\n" +
        "*   **安全第一：** 优先考虑用户安全，不推荐危险或不合规的活动/区域。提供必要的安全预警和注意事项。\n" +
        "*   **预算敏感：** 在推荐时明确标注成本范围（如住宿每晚$XX-$XX，某活动约$XX），并尊重用户的预算框架，提供不同档位的选择。\n" +
        "*   **信息透明与免责：** 如需提供实时信息（如具体票价、即时空房、最新签证政策），在给出信息的同时，明确告知用户这些信息可能随时变化，强烈建议用户通过官方渠道或可靠预订平台进行最终核实和操作。自身不进行实时预订操作。\n" +
        "*   **主动澄清：** 如果用户需求模糊或信息不足，主动提出具体问题以获取更精准的信息，从而提供更贴合的方案。\n" +
        "*   **调用工具：** 当需要提供最新的票价、酒店评价、景点开放状态、政策规定或深度目的地信息时，可以调用搜索工具或查询相关知识库/数据库，并说明信息来源。";

    public LoveApp(ChatModel dashScopeChatModel, ConversationMemoryDao conversationMemoryDao) {
//        String dir = System.getProperty("user.dir") + "/chat-memory";
//        FileBasedChatMemory fileBasedChatMemory = new FileBasedChatMemory(dir);
        MysqlChatMemory mysqlChatMemory = new MysqlChatMemory(conversationMemoryDao);
        chatClient = ChatClient.builder(dashScopeChatModel)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(mysqlChatMemory),
                new SimpleLoggerAdvisors()
            )
            .defaultSystem(STSTEM_RPOMPT)
            .build();
    }

    /**
     * 流式的返回结果
     *
     * @param chatId
     * @param content
     * @return
     */
    public Flux<String> doChatByStream(String chatId, String content) {
        // 将用户的提示词重写
        String rewriter = queryRewriter.doQueryRewriter(content);
        return chatClient
            .prompt()
            .user(rewriter)
            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
            .advisors(loveAppRagCloudAdvisorsConfig.loveAppCloudAdvisors())
            .tools(alltools)
            .tools(toolCallbackProvider)
            .stream()
            .content();
    }

//    /**
//     * Java14
//     *
//     * @param title
//     * @param suggestion
//     */
//    record LoveReport(String title, List<String> suggestion) {
//
//    }

//    public LoveReport doChatWithReport(String message, String chatId) {
//        LoveReport loveReport = chatClient.prompt()
//            .user(message)
//            .system(STSTEM_RPOMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
//            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//            .call().entity(LoveReport.class);
//        log.info(loveReport.toString());
//        return loveReport;
//    }


//    public String doChatWithRag(String message, String chatId) {
//        String rewriter = queryRewriter.doQueryRewriter(message);
//        ChatResponse response = chatClient
//            .prompt()
//            .user(rewriter)
//            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//            .advisors(LoveAppAdvisorsFactory.createInstance(loveAppVectorStoreConfig.pgVectorVectorStore()),
//                new SimpleLoggerAdvisors())
//            .call()
//            .chatResponse();
//        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }

//    public String doChatWithCloudRag(String message, String chatId) {
//        String rewriter = queryRewriter.doQueryRewriter(message);
//        ChatResponse response = chatClient
//            .prompt()
//            .user(rewriter)
//            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//            .advisors(loveAppRagCloudAdvisorsConfig.loveAppCloudAdvisors(),
//                new SimpleLoggerAdvisors())
//            .call()
//            .chatResponse();
//        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }

//    @Resource
//    private ToolCallback[] allTools;
//
//    public String doChatWithTools(String message, String chatId) {
//        ChatResponse response = chatClient
//            .prompt()
//            .user(message)
//            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//            // 开启日志，便于观察效果
//            .advisors(new SimpleLoggerAdvisors())
//            .tools(allTools)
//            .call()
//            .chatResponse();
//        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }

//
//
//
//    public String doChatWithMcp(String message, String chatId) {
//
//        ChatResponse response = chatClient
//            .prompt()
//            .user(message)
//            .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//            // 开启日志，便于观察效果
//            .advisors(new SimpleLoggerAdvisors())
//            .tools(toolCallbackProvider)
//            .call()
//            .chatResponse();
//        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }

}
