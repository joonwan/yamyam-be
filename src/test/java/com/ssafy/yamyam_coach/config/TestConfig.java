package com.ssafy.yamyam_coach.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@TestConfiguration
public class TestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            @Override
            public int dimensions() { return 768; }

            private float[] getFakeVector() {
                float[] vector = new float[768];
                // 0이 아닌 값을 넣어줘야 Redis가 유사도를 계산할 수 있습니다.
                java.util.Arrays.fill(vector, 0.1f);
                return vector;
            }

            @Override
            public float[] embed(String text) {
                return getFakeVector();
            }

            @Override
            public float[] embed(Document document) {
                return getFakeVector();
            }

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = request.getInstructions().stream()
                        .map(text -> new Embedding(getFakeVector(), 0))
                        .toList();
                return new EmbeddingResponse(embeddings);
            }
        };
    }
}
