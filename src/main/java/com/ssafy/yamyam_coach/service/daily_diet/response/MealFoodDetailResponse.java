package com.ssafy.yamyam_coach.service.daily_diet.response;

import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import lombok.Builder;

public class MealFoodDetailResponse {

    private Long foodId;
    private String foodName;
    private Long mealFoodID;
    private Double quantity;
    private BaseUnit unit;
    private Double energyPer100;

    @Builder
    private MealFoodDetailResponse(Long foodId, String foodName, Long mealFoodID, Double quantity, BaseUnit unit, Double energyPer100) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.mealFoodID = mealFoodID;
        this.quantity = quantity;
        this.unit = unit;
        this.energyPer100 = energyPer100;
    }
}
