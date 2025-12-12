package com.ssafy.yamyam_coach.repository.meal.response;

import com.ssafy.yamyam_coach.domain.meals.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MealDetail {
    private Long id;                                    // meal id
    private MealType type;                              // 아침, 점심, 저녁, 간식 타입
    private Long dailyDietId;                           // 어떤 일일 식단에 속하는지
    private List<MealFoodDetail> mealFoods;             // MealFood 교차테이블 상세 정보들
}
