package com.ssafy.yamyam_coach.service.meal.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
public class CreateMealFoodServiceRequest {

    @NotNull(message = "food id 는 필수 입니다.")
    private Long foodId;

    @NotNull(message = "양은 필수 입니다.")
    @Positive(message = "양은 반드시 양수여야 합니다.")
    private Double amount;

    @Builder
    private CreateMealFoodServiceRequest(Long foodId, Double amount) {
        this.foodId = foodId;
        this.amount = amount;
    }
}
