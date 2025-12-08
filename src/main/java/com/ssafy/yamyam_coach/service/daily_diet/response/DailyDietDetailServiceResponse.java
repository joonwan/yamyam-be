package com.ssafy.yamyam_coach.service.daily_diet.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class DailyDietDetailServiceResponse {

    private Long dailyDietId;
    private boolean isEmpty;
    private String description;
    private List<MealFoodDetailServiceResponse> breakfast;
    private List<MealFoodDetailServiceResponse> lunch;
    private List<MealFoodDetailServiceResponse> dinner;
    private List<MealFoodDetailServiceResponse> snack;

    @Builder
    private DailyDietDetailServiceResponse(Long dailyDietId, boolean isEmpty, String description, List<MealFoodDetailServiceResponse> breakfast, List<MealFoodDetailServiceResponse> lunch, List<MealFoodDetailServiceResponse> dinner, List<MealFoodDetailServiceResponse> snack) {
        this.dailyDietId = dailyDietId;
        this.isEmpty = isEmpty;
        this.description = description;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.snack = snack;
    }
}
