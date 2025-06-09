//package com.example.aiagent.rag;
//
//
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
//import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
//import org.springframework.ai.vectorstore.SimpleVectorStore;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.lang.ref.PhantomReference;
//import java.util.List;
//
//import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
//import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;
//
///**
// * 基于spring AI自带的向量存数和阿里Embedding模型
// */
//@Configuration
//public class LoveAppVectorStoreConfig {
//
//    @Resource
//    private MyKeywordEnricher myKeywordEnricher;
//
//    @Resource
//    private LoveAppDocumentLoader loveAppDocumentLoader;
//
//    @Resource
//    private JdbcTemplate jdbcTemplate;
//
//    @Resource
//    private EmbeddingModel dashscopeEmbeddingModel;
//
//
//    @Bean
//    public VectorStore pgVectorVectorStore() {
//        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
//            .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
//            .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
//            .indexType(HNSW)                     // Optional: defaults to HNSW
//            .initializeSchema(true)              // Optional: defaults to false
//            .schemaName("public")                // Optional: defaults to "public"
//            .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
//            .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
//            .build();
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        // 添加元信息
//        List<Document> enricherDocument = myKeywordEnricher.enricherDocument(documents);
//        vectorStore.add(enricherDocument);
//        return vectorStore;
//    }
//}
