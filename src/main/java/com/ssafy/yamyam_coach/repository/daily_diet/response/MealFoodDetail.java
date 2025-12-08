package com.ssafy.yamyam_coach.repository.daily_diet.response;

import com.ssafy.yamyam_coach.domain.food.Food;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MealFoodDetail {
    private Long id;
    private Double quantity;
    private Food food;
}