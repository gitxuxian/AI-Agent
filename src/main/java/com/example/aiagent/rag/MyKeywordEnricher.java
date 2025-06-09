package com.example.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 给文档生成更多的metadata
 */
@Component
public class MyKeywordEnricher {
    @Resource
    private ChatModel dashscopChatModel;

    List<Document> enricherDocument(List<Document> documentList) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(this.dashscopChatModel, 5);
        return keywordMetadataEnricher.apply(documentList);
    }
}
