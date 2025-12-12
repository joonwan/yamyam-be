package com.ssafy.yamyam_coach.service.meal.request;

import com.ssafy.yamyam_coach.domain.meals.MealType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMealServiceRequest{

    private Long mealId;
    private MealType mealType;
    private List<UpdateMealFoodServiceRequest> mealFoodUpdateRequests;

    @Builder
    private UpdateMealServiceRequest(Long mealId, MealType mealType, List<UpdateMealFoodServiceRequest> mealFoodUpdateRequests) {
        this.mealId = mealId;
        this.mealType = mealType;
        this.mealFoodUpdateRequests = mealFoodUpdateRequests;
    }
}
