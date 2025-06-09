package com.example.aiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写
 */
@Component
public class QueryRewriter {

    private RewriteQueryTransformer rewriteQueryTransformer;

    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        rewriteQueryTransformer = RewriteQueryTransformer.builder().chatClientBuilder(builder).build();
    }

    public String doQueryRewriter(String prompt) {
        Query query = new Query(prompt);
        Query transform = rewriteQueryTransformer.transform(query);
        return transform.text();
    }

}
