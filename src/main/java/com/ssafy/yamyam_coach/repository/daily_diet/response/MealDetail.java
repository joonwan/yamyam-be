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
    private Long id;
    private MealType type;
    private List<MealFoodDetail> mealFoods;
}
