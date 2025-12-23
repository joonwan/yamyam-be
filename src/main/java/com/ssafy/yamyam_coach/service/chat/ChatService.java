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
            
            [필수 지시사항]
            사용자의 질문이 '예측'을 요구할 경우, **반드시 구체적인 수치(예상 몸무게, 날짜, 감량 kg 등)**를 포함하여 답변하세요.
            데이터가 부족하다면 일반적인 성인 남/녀 기초대사량을 가정하고, '섭취 칼로리 - 소모 칼로리' 계산을 통해 논리적으로 추정하세요. (예: 7,000kcal 결손 시 체중 1kg 감소로 가정)
            예측을 요구하지 않을 때 꼭 예측을 할 필요는 없습니다.
            
            ★ 4. [예외 허용]: '연예인 비교', '단순 흥미 위주 질문', '일반적인 상식'에 대한 질문은
            제공된 데이터에 없더라도 당신이 가진 **외부 지식(사전 학습된 데이터)**을 활용하여 자유롭고 재치 있게 답변하세요.
            (예: "데이터엔 없지만, 제 기억엔 배우 OOO님이 회원님과 비슷한 피지컬이네요!")
            
            마크다운 형식으로 구조화 해서 답변해주세요.
            
            표를 생성할 수 있다면 표를 생성해주세요.
            
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
        // 1. 식단 정보가 없거나, 상세 음식 리스트가 비어있으면 종료
        if (meal == null || meal.getMealFoods() == null || meal.getMealFoods().isEmpty()) {
            return;
        }

        // 2. 총 영양소 계산을 위한 변수 초기화
        double totalCalories = 0.0;
        double totalCarbo = 0.0; // 탄수화물
        double totalProtein = 0.0; // 단백질
        double totalFat = 0.0; // 지방

        // 3. 리스트를 순회하며 총합 계산 (헤더를 먼저 찍어야 해서 계산이 선행되어야 함)
        for (MealFoodDetailResponse food : meal.getMealFoods()) {
            // NullPointerException 방지 (0.0 처리)
            double quantity = food.getQuantity() != null ? food.getQuantity() : 0.0;
            double ratio = quantity / 100.0; // 섭취 비율 (100g 기준)

            // 각 영양소 데이터 null 체크
            double energy = food.getEnergyPer100() != null ? food.getEnergyPer100() : 0.0;
            double carbo = food.getCarbohydratePer100() != null ? food.getCarbohydratePer100() : 0.0;
            double protein = food.getProteinPer100() != null ? food.getProteinPer100() : 0.0;
            double fat = food.getFatPer100() != null ? food.getFatPer100() : 0.0;

            // 누적 계산
            totalCalories += ratio * energy;
            totalCarbo += ratio * carbo;
            totalProtein += ratio * protein;
            totalFat += ratio * fat;
        }

        // 4. 헤더 출력 (총 칼로리 + 탄단지 정보 포함)
        // 예: "  [아침] (총 520kcal | 탄: 50g, 단: 20g, 지: 10g)"
        sb.append(String.format("  [%s] (총 %.0fkcal | 탄수화물: %.0fg, 단백질: %.0fg, 지방: %.0fg)\n",
                mealName, totalCalories, totalCarbo, totalProtein, totalFat));

        // 5. 상세 음식 리스트 출력
        for (MealFoodDetailResponse food : meal.getMealFoods()) {
            double quantity = food.getQuantity() != null ? food.getQuantity() : 0.0;
            double energyPer100 = food.getEnergyPer100() != null ? food.getEnergyPer100() : 0.0;

            // 개별 음식 칼로리
            double foodCalories = (quantity / 100.0) * energyPer100;

            // 예: "    - 현미밥 210g (300kcal)"
            sb.append(String.format("    - %s %.0f%s (%.0fkcal)\n",
                    food.getFoodName(),
                    quantity,
                    food.getUnit(),
                    foodCalories
            ));
        }
    }
}