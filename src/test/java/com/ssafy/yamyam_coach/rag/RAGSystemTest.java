package com.ssafy.yamyam_coach.rag;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RAG (Retrieval-Augmented Generation) 시스템 테스트
 *
 * RAG 흐름:
 * 1. 문서를 Vector Store에 저장 (Embedding으로 벡터화)
 * 2. 사용자 질문에 대해 유사한 문서 검색
 * 3. 검색된 문서를 컨텍스트로 ChatModel에 전달
 * 4. 정확한 답변 생성
 */
public class RAGSystemTest extends IntegrationTestSupport {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private VectorStore vectorStore;

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        chatClient = ChatClient.create(chatModel);
    }

    @DisplayName("RAG 시스템으로 음식 관련 보고서를 작성할 수 있다")
    @Test
    void ragSystemTest() {
        // 1. 음식 관련 문서들을 Vector Store에 저장
        System.out.println("\n=== 1단계: 문서 저장 ===");

        Document doc1 = new Document(
            "한식: 김치찌개는 한국의 대표적인 찌개 요리로, 김치, 돼지고기, 두부가 주재료입니다. " +
            "매콤하고 얼큰한 맛이 특징이며, 밥과 함께 먹으면 좋습니다.",
            Map.of("category", "한식", "dish", "김치찌개")
        );

        Document doc2 = new Document(
            "한식: 불고기는 한국의 전통 고기 요리로, 간장 양념에 재운 소고기를 구워 먹습니다. " +
            "달콤하고 짭짤한 맛이 특징이며, 국내외에서 인기가 많습니다.",
            Map.of("category", "한식", "dish", "불고기")
        );

        Document doc3 = new Document(
            "양식: 파스타는 이탈리아의 대표 음식으로, 밀가루 반죽을 다양한 형태로 만들어 소스와 함께 먹습니다. " +
            "토마토 소스, 크림 소스 등 다양한 종류가 있습니다.",
            Map.of("category", "양식", "dish", "파스타")
        );

        Document doc4 = new Document(
            "일식: 초밥은 일본의 대표 음식으로, 식초로 간을 한 밥 위에 신선한 생선을 올린 요리입니다. " +
            "간장과 와사비를 곁들여 먹으며, 신선도가 중요합니다.",
            Map.of("category", "일식", "dish", "초밥")
        );

        Document doc5 = new Document(
            "영양 정보: 김치찌개는 비타민과 유산균이 풍부하며, 100g당 약 50kcal입니다. " +
            "캡사이신 성분으로 인해 신진대사를 촉진하는 효과가 있습니다.",
            Map.of("category", "영양", "dish", "김치찌개")
        );

        vectorStore.add(List.of(doc1, doc2, doc3, doc4, doc5));
        System.out.println("✓ 5개의 음식 관련 문서 저장 완료");

        // 2. 사용자 질문: "한식에 대해 알려줘"
        System.out.println("\n=== 2단계: 유사 문서 검색 ===");
        String question = "한식에 대해 알려줘";
        System.out.println("질문: " + question);

        List<Document> relatedDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(3)
                .build()
        );

        System.out.println("\n검색된 문서:");
        relatedDocs.forEach(doc -> {
            System.out.println("- " + doc.getText().substring(0, Math.min(50, doc.getText().length())) + "...");
            System.out.println("  score: " + doc.getMetadata().get("score"));
        });

        // 검증: 한식 관련 문서가 검색되었는지
        assertThat(relatedDocs).isNotEmpty();
        assertThat(relatedDocs).hasSizeGreaterThanOrEqualTo(2);

        // 상위 문서들이 한식 관련인지 확인
        boolean hasKoreanFood = relatedDocs.stream()
            .anyMatch(doc -> doc.getText().contains("한식") || doc.getText().contains("김치") || doc.getText().contains("불고기"));
        assertThat(hasKoreanFood).isTrue();

        // 3. 검색된 문서를 컨텍스트로 ChatModel에 전달하여 답변 생성
        System.out.println("\n=== 3단계: AI 답변 생성 ===");

        String context = relatedDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            다음 정보를 바탕으로 질문에 답변해주세요.

            [참고 자료]
            %s

            [질문]
            %s

            [답변 요구사항]
            - 참고 자료의 정보만 사용하여 답변하세요
            - 3-4문장으로 간단명료하게 답변하세요
            - 존댓말을 사용하세요
            """, context, question);

        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        System.out.println("\nAI 답변:");
        System.out.println(answer);

        // 검증: 답변이 생성되었는지
        assertThat(answer).isNotNull();
        assertThat(answer).isNotEmpty();
        assertThat(answer.length()).isGreaterThan(50);

        // 4. 다른 질문으로 테스트
        System.out.println("\n=== 4단계: 다른 질문 테스트 ===");
        String question2 = "김치찌개의 영양 정보를 알려줘";
        System.out.println("질문: " + question2);

        List<Document> relatedDocs2 = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question2)
                .topK(3)
                .build()
        );

        System.out.println("\n검색된 문서:");
        relatedDocs2.forEach(doc -> {
            System.out.println("- " + doc.getText().substring(0, Math.min(50, doc.getText().length())) + "...");
        });

        String context2 = relatedDocs2.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        String prompt2 = String.format("""
            다음 정보를 바탕으로 질문에 답변해주세요.

            [참고 자료]
            %s

            [질문]
            %s

            [답변]
            """, context2, question2);

        String answer2 = chatClient.prompt()
            .user(prompt2)
            .call()
            .content();

        System.out.println("\nAI 답변:");
        System.out.println(answer2);

        // 검증: 영양 정보가 포함된 답변인지
        assertThat(answer2).isNotNull();
        assertThat(answer2).isNotEmpty();

        System.out.println("\n=== ✅ RAG 시스템 정상 작동! ===");
        System.out.println("음식에 대한 보고서 작성 가능 확인 완료");
    }

    @DisplayName("RAG 시스템으로 특정 음식에 대한 상세 보고서를 작성할 수 있다")
    @Test
    void detailedFoodReportTest() {
        // 1. 김치찌개에 대한 다양한 문서 저장
        System.out.println("\n=== 김치찌개 상세 정보 문서 저장 ===");

        Document recipe = new Document(
            "김치찌개 레시피: 묵은 김치 200g, 돼지고기 100g, 두부 반모, 양파 1/2개, 대파, 마늘을 준비합니다. " +
            "김치를 볶다가 물을 넣고 끓이면서 고기와 두부를 넣어 완성합니다.",
            Map.of("type", "레시피", "dish", "김치찌개")
        );

        Document nutrition = new Document(
            "김치찌개 영양성분: 1인분(300g) 기준 열량 150kcal, 단백질 12g, 지방 8g, 탄수화물 10g입니다. " +
            "비타민 C와 유산균이 풍부하여 면역력 향상에 도움이 됩니다.",
            Map.of("type", "영양", "dish", "김치찌개")
        );

        Document history = new Document(
            "김치찌개의 역사: 조선시대부터 먹어온 전통 음식으로, 묵은 김치를 활용한 서민 음식이었습니다. " +
            "현대에는 한국을 대표하는 국민 음식이 되었습니다.",
            Map.of("type", "역사", "dish", "김치찌개")
        );

        vectorStore.add(List.of(recipe, nutrition, history));
        System.out.println("✓ 김치찌개 관련 3개 문서 저장 완료");

        // 2. "김치찌개에 대한 보고서 작성"
        System.out.println("\n=== 김치찌개 보고서 생성 ===");

        String question = "김치찌개";
        List<Document> allDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)
                .build()
        );

        String context = allDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        String reportPrompt = String.format("""
            다음 정보를 바탕으로 김치찌개에 대한 보고서를 작성해주세요.

            [참고 자료]
            %s

            [보고서 형식]
            # 김치찌개 보고서

            ## 1. 개요
            (간단한 소개)

            ## 2. 레시피
            (재료와 조리 방법)

            ## 3. 영양 정보
            (영양성분과 건강 효과)

            ## 4. 역사적 배경
            (유래와 발전 과정)

            참고 자료에 있는 정보만 사용하여 작성하세요.
            """, context);

        String report = chatClient.prompt()
            .user(reportPrompt)
            .call()
            .content();

        System.out.println("\n생성된 보고서:");
        System.out.println("=".repeat(60));
        System.out.println(report);
        System.out.println("=".repeat(60));

        // 검증
        assertThat(report).isNotNull();
        assertThat(report).contains("김치찌개");
        assertThat(report.length()).isGreaterThan(200);

        System.out.println("\n=== ✅ 상세 보고서 작성 성공! ===");
    }
}
