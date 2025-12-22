package com.ssafy.yamyam_coach.global.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost; //default localhost

    @Value("${spring.data.redis.port:6379}")
    private int redisPort; //default 6379

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {

        return RedisVectorStore.builder(new JedisPooled(redisHost, redisPort), embeddingModel)
                .initializeSchema(true)  // Schema 초기화 여부 (default: false)
                .metadataFields(  // Filtering을 위한 metadata field 설정
                        MetadataField.tag("category"),
                        MetadataField.numeric("meta_num"),
                        MetadataField.text("meta_txt")
                )
                .build();
    }
}
