//package com.example.aiagent.advisors;
//
//import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
//import org.springframework.ai.chat.client.advisor.api.Advisor;
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
//import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
//import org.springframework.stereotype.Component;
//
//
///**
// * 文档检索高级实例
// */
//@Component
//public class LoveAppAdvisorsFactory {
//
//    public static Advisor createInstance(VectorStore vectorStore) {
//        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
//
//            抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
//
//            """);
//        var b = new FilterExpressionBuilder();
//        // 创建主题过滤器：只查询单身、恋爱、婚姻这三大主题
//        var filterExpression = b.in("metadata.topic", "单身", "恋爱", "婚姻")
//            .build();
//        //查询增强顾问
//        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//            //上下文感知
//            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true)
//                .emptyContextPromptTemplate(emptyContextPromptTemplate).build())
//            //文档检索器
//            .documentRetriever(
//                VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
//                    .topK(3).filterExpression(filterExpression).similarityThreshold(0.5).build()).build();
//        return retrievalAugmentationAdvisor;
//    }
//}