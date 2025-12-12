package com.ssafy.yamyam_coach.repository.meal.response;

import com.ssafy.yamyam_coach.domain.food.Food;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MealFoodDetail {
    private Long id;                // meal food id
    private Double quantity;        // 양
    private Food food;              // 음식 세부 정보
}
