package com.ssafy.yamyam_coach.service.daily_diet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyDietDetailResponse {

    private Long dailyDietId;
    private LocalDate date;
    private String dayOfWeek;
    private String description;

    private List<MealFoodDetailResponse> breakfast;
    private List<MealFoodDetailResponse> lunch;
    private List<MealFoodDetailResponse> dinner;
    private List<MealFoodDetailResponse> snack;

    @Builder
    private DailyDietDetailResponse(Long dailyDietId, LocalDate date, String dayOfWeek, String description, List<MealFoodDetailResponse> breakfast, List<MealFoodDetailResponse> lunch, List<MealFoodDetailResponse> dinner, List<MealFoodDetailResponse> snack) {
        this.dailyDietId = dailyDietId;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.description = description;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.snack = snack;
    }
}
