package com.ssafy.yamyam_coach.service.daily_diet.response;

import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import lombok.Builder;
import lombok.Data;

@Data
public class MealFoodDetailServiceResponse {

    private Long mealFoodId;
    private Long foodId;
    private String name;
    private Double amount;
    private BaseUnit unit;
    private Double caloriePerG;
    private Double caloriePerMl;

    @Builder
    private MealFoodDetailServiceResponse(Long mealFoodId, Long foodId, String name, Double amount, BaseUnit unit, Double caloriePerG, Double caloriePerMl) {
        this.mealFoodId = mealFoodId;
        this.foodId = foodId;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.caloriePerG = caloriePerG;
        this.caloriePerMl = caloriePerMl;
    }
}
