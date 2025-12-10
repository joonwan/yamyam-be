package com.ssafy.yamyam_coach.repository.daily_diet.response;

import com.ssafy.yamyam_coach.domain.meals.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MealDetail {
    private Long id;                                // mea id
    private MealType type;                          // 아침, 점심, 저녁, 간식 타입
    private List<MealFoodDetail> mealFoods;         // MealFood 교차테이블 상세 정보들
}
