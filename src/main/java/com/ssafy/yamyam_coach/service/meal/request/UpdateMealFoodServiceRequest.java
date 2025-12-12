package com.ssafy.yamyam_coach.service.meal.request;

import lombok.Builder;
import lombok.Data;

@Data
public class UpdateMealFoodServiceRequest {
    private Long foodId;
    private Double amount;

    @Builder
    private UpdateMealFoodServiceRequest(Long foodId, Double amount) {
        this.foodId = foodId;
        this.amount = amount;
    }
}
