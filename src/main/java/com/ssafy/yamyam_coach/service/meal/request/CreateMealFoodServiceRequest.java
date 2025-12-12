package com.ssafy.yamyam_coach.service.meal.request;

import lombok.Data;

@Data
public class CreateMealFoodRequest {

    private Long foodId;
    private Double amount;

}
