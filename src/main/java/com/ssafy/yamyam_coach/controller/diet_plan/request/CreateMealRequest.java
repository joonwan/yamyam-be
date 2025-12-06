package com.ssafy.yamyam_coach.controller.diet_plan.request;

import com.ssafy.yamyam_coach.domain.food.BaseUnit;
import lombok.Data;

@Data
public class CreateMealRequest {

    private Long foodId;
    private Double amount;
    private BaseUnit unit;

}
