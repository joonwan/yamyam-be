package com.ssafy.yamyam_coach.service.meal.request;

import com.ssafy.yamyam_coach.domain.meals.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class CreateMealServiceRequest {

    @NotNull(message = "daily diet id 는 필수 입니다.")
    private Long dailyDietId;

    @NotNull(message = "meal type 은 필수 입니다.")
    private MealType mealType;

    @Valid
    @NotNull(message = "식단 음식은 null 일 수 없습니다.")
    private List<CreateMealFoodServiceRequest> mealFoodRequests;

    @Builder
    private CreateMealServiceRequest(Long dailyDietId, MealType mealType, List<CreateMealFoodServiceRequest> mealFoodRequests) {
        this.dailyDietId = dailyDietId;
        this.mealType = mealType;
        this.mealFoodRequests = mealFoodRequests;
    }
}
