package com.ssafy.yamyam_coach.vector;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentVectorStoreTest extends IntegrationTestSupport {

    @Autowired
    private VectorStore vectorStore;

    @DisplayName("vector store 에 문서를 저장할 수 있다.")
    @Test
    void insert() {

        // given
        Document doc1 = new Document("치킨 맛있어", Map.of("category", "한식"));
        Document doc2 = new Document("로봇", Map.of("category", "산업"));
        Document doc3 = new Document("연필", Map.of("category", "필기구"));

        // when
        vectorStore.add(List.of(doc1, doc2, doc3));
        List<Document> result = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("음식")
                        .topK(2)
                        .build()
        );

        // then
        assertThat(result).hasSize(2);
        System.out.println("result = " + result);
    }
}
