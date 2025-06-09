package com.example.aiagent.advisors;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于百炼平台的RAG存储
 */
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorsConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;


    public Advisor loveAppCloudAdvisors() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
            
            抱歉，我只能回答旅行相关的问题，你可以去到智能体应用，他更加的强大
            
            """);
        DashScopeApi dashScopeAgentApi = new DashScopeApi(dashScopeApiKey);
        final String KONWLEDGE_INDEX = "旅行助手";
        DashScopeDocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeAgentApi,
            DashScopeDocumentRetrieverOptions.builder().withIndexName(KONWLEDGE_INDEX)
                .build());
        return RetrievalAugmentationAdvisor.builder().documentRetriever(dashScopeDocumentRetriever)
            // 配置查询增强器
            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true)
                .emptyContextPromptTemplate(emptyContextPromptTemplate).build())
            .build();
    }
}
