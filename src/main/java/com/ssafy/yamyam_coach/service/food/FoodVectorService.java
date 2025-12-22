package com.ssafy.yamyam_coach.service.food;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FoodVectorService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public void syncAllFoods() {
        log.info("=== [ETL] 고품질 JSON 데이터 기반 벡터 적재 시작 ===");

        try {
            // 1. JSON 파일 로드 (resources/foods.json)
            Resource resource = resourceLoader.getResource("classpath:food_nutrition_data.json");
            FoodDataRoot dataRoot = objectMapper.readValue(resource.getInputStream(), new TypeReference<FoodDataRoot>() {});

            List<FoodItem> foodItems = dataRoot.getFoods();
            if (foodItems == null || foodItems.isEmpty()) {
                log.warn("적재할 데이터가 JSON 내에 존재하지 않습니다.");
                return;
            }

            log.info("총 {}개의 음식을 처리합니다.", foodItems.size());

            // 2. Document 객체로 변환
            List<Document> documents = foodItems.stream()
                    .map(this::convertToDocument)
                    .toList();

            // 3. Batch 적재 (메모리 및 네트워크 안정성을 위해 100개 단위 처리)
            int batchSize = 100;
            int totalSize = documents.size();
            for (int i = 0; i < totalSize; i += batchSize) {
                int end = Math.min(i + batchSize, totalSize);
                vectorStore.add(documents.subList(i, end));
                log.info("진행 중... {}/{} 적재 완료", end, totalSize);
            }

            log.info("=== [ETL] 완료! 총 {}개의 고품질 데이터가 적재되었습니다. ===", totalSize);

        } catch (IOException e) {
            log.error("JSON 파일을 읽는 중 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("식품 데이터 동기화 실패", e);
        }
    }

    /**
     * JSON 아이템을 Spring AI Document로 변환합니다.
     * content에 nutritionNote를 결합하여 검색 시 의미적 연관성을 강화합니다.
     */
    private Document convertToDocument(FoodItem item) {
        Map<String, Object> metadata = item.getMetadata();

        // 1. 검색 품질을 위해 원본 content에 전문가의 nutritionNote를 결합
        String originalContent = item.getContent();
        String nutritionNote = (String) metadata.getOrDefault("nutritionNote", "");

        String enhancedContent = originalContent;
        if (!nutritionNote.isEmpty()) {
            enhancedContent += " [영양 전문가 분석]: " + nutritionNote;
        }

        // 2. Document 생성 (ID 유지 및 전체 메타데이터 포함)
        return new Document(item.getId(), enhancedContent, metadata);
    }

    /**
     * JSON 구조 매핑을 위한 내부 DTO 클래스
     */
    @Data
    public static class FoodDataRoot {
        private List<FoodItem> foods;
        private Map<String, Object> metadata;
    }

    @Data
    public static class FoodItem {
        private String id;
        private Map<String, Object> metadata;
        private String content;
    }
}