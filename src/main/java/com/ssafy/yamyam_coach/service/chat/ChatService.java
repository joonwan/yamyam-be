package com.ssafy.yamyam_coach.service.chat;

import com.ssafy.yamyam_coach.controller.chat.request.ChatRequest;
import com.ssafy.yamyam_coach.domain.body_spec.BodySpec;
import com.ssafy.yamyam_coach.domain.challenge.Challenge;
import com.ssafy.yamyam_coach.domain.daily_diet.DailyDiet;
import com.ssafy.yamyam_coach.repository.body_spec.BodySpecRepository;
import com.ssafy.yamyam_coach.repository.challenge.ChallengeRepository;
import com.ssafy.yamyam_coach.repository.daily_diet.DailyDietRepository;
import com.ssafy.yamyam_coach.service.daily_diet.DailyDietService;
import com.ssafy.yamyam_coach.service.daily_diet.response.DailyDietDetailResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.MealDetailResponse;
import com.ssafy.yamyam_coach.service.daily_diet.response.MealFoodDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final String promptTemplate = """
            당신은 '냠냠코치' 프로젝트의 영양 전문가 쩝쩝 교수 입니다.
            아래에 제공된 [참고 정보]를 바탕으로 사용자의 질문에 답변해 주세요.
            답변을 모를 경우 억지로 지어내지 말고 모른다고 답변하세요.
            
            아래 제공된 [배경 지식]과 [사용자 데이터]를 바탕으로 질문에 답변해 주세요.
            
            [배경 지식 (매뉴얼/영양 정보)]
            {rag_context}
            
            [사용자 데이터 (선택된 기록)]
            {user_context}
            
            [사용자 질문]
            {question}
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    private final DailyDietService dailyDietService;
    // Repository 주입
    private final BodySpecRepository bodySpecRepository;
    private final DailyDietRepository dailyDietRepository;
    private final ChallengeRepository challengeRepository;

    public String request(Long userId, ChatRequest request) {
        // 1. [RAG] 벡터 DB 검색
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.getContent())
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build()
        );

        String ragContext = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 2. [Context Injection] 선택된 ID로 DB 조회 -> 문자열 변환
        String userContext = buildUserContext(request);

        // 3. [LLM] AI 호출
        return chatClient.prompt()
                .user(u -> u.text(promptTemplate)
                        .param("rag_context", ragContext.isEmpty() ? "관련 배경 지식 없음" : ragContext)
                        .param("user_context", userContext.isEmpty() ? "선택된 데이터 없음" : userContext)
                        .param("question", request.getContent())
                )
                .call()
                .content();
    }

    // 데이터를 문자열로 변환하는 로직
    private String buildUserContext(ChatRequest req) {
        StringBuilder sb = new StringBuilder();

        // A. 신체 정보
        if (req.getBodySpecIds() != null && !req.getBodySpecIds().isEmpty()) {
            List<BodySpec> specs = bodySpecRepository.findAllById(req.getBodySpecIds());
            sb.append("[신체 정보]\n");
            for (BodySpec s : specs) {
                sb.append(String.format("- 날짜: %s, 키: %s, 체중: %s\n", s.getCreatedAt(), s.getHeight(), s.getWeight()));
            }
            sb.append("\n");
        }

        // B. 식단 정보
        if (req.getDailyDietIds() != null && !req.getDailyDietIds().isEmpty()) {
            List<DailyDietDetailResponse> responses = dailyDietService.getDailyDietListByIds(req.getDailyDietIds());

            sb.append("[식단 기록]\n");
            for (DailyDietDetailResponse res : responses) {
                // 날짜와 요일, 메모 출력
                sb.append(String.format("📅 날짜: %s (%s)", res.getDate(), res.getDayOfWeek()));
                if (res.getDescription() != null) sb.append(" - 메모: ").append(res.getDescription());
                sb.append("\n");

                // 끼니별 상세 정보 출력 (코드가 깔끔해짐)
                appendMealInfo(sb, "아침", res.getBreakfast());
                appendMealInfo(sb, "점심", res.getLunch());
                appendMealInfo(sb, "저녁", res.getDinner());
                appendMealInfo(sb, "간식", res.getSnack());

                sb.append("\n"); // 하루 기록 끝마다 줄바꿈
            }
        }

        // C. 챌린지 정보
        if (req.getChallengeIds() != null && !req.getChallengeIds().isEmpty()) {
            List<Challenge> challenges = challengeRepository.findAllById(req.getChallengeIds());
            sb.append("[참여 챌린지]\n");
            for (Challenge c : challenges) {
                sb.append(String.format("- %s (상태: %s)\n", c.getTitle(), c.getStatus()));
            }
        }

        return sb.toString();
    }

    private void appendMealInfo(StringBuilder sb, String mealName, MealDetailResponse meal) {
        // 1. 식단 정보가 없거나, 상세 음식 리스트가 비어있으면 아무것도 출력 안 하고 종료
        if (meal == null || meal.getMealFoods() == null || meal.getMealFoods().isEmpty()) {
            return;
        }

        // 2. 총 칼로리 계산 (DTO에 합계 필드가 없으므로 직접 계산해야 함)
        double totalMealCalories = 0.0;

        for (MealFoodDetailResponse food : meal.getMealFoods()) {
            // NullPointerException 방지를 위한 안전한 값 추출 (0.0 처리)
            double quantity = food.getQuantity() != null ? food.getQuantity() : 0.0;
            double energyPer100 = food.getEnergyPer100() != null ? food.getEnergyPer100() : 0.0;

            // 칼로리 공식: (섭취량 / 100) * 100g당 칼로리
            totalMealCalories += (quantity / 100.0) * energyPer100;
        }

        // 3. 헤더 출력 -> 예: "  [아침] (총 520kcal)"
        sb.append(String.format("  [%s] (총 %.0fkcal)\n", mealName, totalMealCalories));

        // 4. 상세 음식 리스트 출력
        for (MealFoodDetailResponse food : meal.getMealFoods()) {
            double quantity = food.getQuantity() != null ? food.getQuantity() : 0.0;
            double energyPer100 = food.getEnergyPer100() != null ? food.getEnergyPer100() : 0.0;

            // 개별 음식 칼로리 계산
            double foodCalories = (quantity / 100.0) * energyPer100;

            // 예: "    - 현미밥 210g (300kcal)"
            // baseUnit은 Enum일 경우 .toString()이 호출됨 (예: GRAM -> "GRAM" or "g")
            sb.append(String.format("    - %s %.0f%s (%.0fkcal)\n",
                    food.getFoodName(),   // 음식 이름
                    quantity,             // 섭취량
                    food.getUnit(),   // 단위
                    foodCalories          // 계산된 칼로리
            ));
        }
    }
}